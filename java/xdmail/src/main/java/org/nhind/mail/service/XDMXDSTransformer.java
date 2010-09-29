/* 
 * Copyright (c) 2010, NHIN Direct Project
 * All rights reserved.
 *  
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution.  
 * 3. Neither the name of the the NHIN Direct Project (nhindirect.org)
 *    nor the names of its contributors may be used to endorse or promote products 
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY 
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND 
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.nhind.mail.service;

import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType.Document;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.bind.JAXBElement;

import oasis.names.tc.ebxml_regrep.xsd.lcm._3.SubmitObjectsRequest;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ExternalIdentifierType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ExtrinsicObjectType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.IdentifiableType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.RegistryObjectListType;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nhind.mail.util.MimeType;
import org.nhind.mail.util.XMLUtils;

/**
 * Class for handling the transformation of XDM to XDS.
 * 
 * @author vlewis
 */
public class XDMXDSTransformer {

    private static final String XDM_FILENAME_DATA = "DOCUMENT.xml";
    private static final String XDM_FILENAME_METADATA = "METADATA.xml";

    /**
     * Class logger.
     */
    private static final Log LOGGER = LogFactory.getFactory().getInstance(XDMXDSTransformer.class);
    
    /**
     * Reads an XDM ZIP archive and returns an XDS submission.
     * 
     * @param dh
     *            The DataHandler object.
     * @return a ProvideAndRegisterDocumentSetRequestType object.
     * @throws Exception
     */
    public ProvideAndRegisterDocumentSetRequestType getXDMRequest(DataHandler dh) throws Exception {
        LOGGER.info("Inside getXDMRequest(DataHandler)");

        File archiveFile = fileFromDataHandler(dh);
        ProvideAndRegisterDocumentSetRequestType request = getXDMRequest(archiveFile);
        
        boolean delete = archiveFile.delete();
        
        if (delete)
            LOGGER.info("Deleted temporary work file " + archiveFile.getAbsolutePath());
        else
            LOGGER.warn("Unable to delete temporary work file " + archiveFile.getAbsolutePath());
        
        return request;
    }

    /**
     * Reads an XDM ZIP archive and returns an XDS submission.
     * 
     * @param archiveFile
     *            The archive file.
     * @return a ProvideAndRegisterDocumentSetRequestType object.
     * @throws Exception
     */
    public ProvideAndRegisterDocumentSetRequestType getXDMRequest(File archiveFile) throws Exception {
        LOGGER.info("Inside getXDMRequest(File)");
        
        String docId = null;
        ZipFile zipFile = null;
        ProvideAndRegisterDocumentSetRequestType prsr = new ProvideAndRegisterDocumentSetRequestType();
        
        try {
            zipFile = new ZipFile(archiveFile, ZipFile.OPEN_READ);

            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            ZipEntry zipEntry = null;

            // load the ZIP archive into memory
            while (zipEntries.hasMoreElements()) {
                LOGGER.trace("in zipEntries");
                zipEntry = zipEntries.nextElement();
                String zname = zipEntry.getName();
                if (!zipEntry.isDirectory()) { //&& zipEntry.getName().startsWith(XDM_DIRSPEC_SUBMISSIONROOT)) {
                    String subsetDirspec = getSubmissionSetDirspec(zipEntry.getName());
                    if (matchName(zname, subsetDirspec, XDM_FILENAME_METADATA)) {
                        InputStream in = zipFile.getInputStream(zipEntry);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        int bytesRead = 0;
                        byte[] buffer = new byte[2048];
                        while ((bytesRead = in.read(buffer)) != -1) {
                            baos.write(buffer, 0, bytesRead);
                        }
                        in.close();
                        LOGGER.trace("metadata " + baos.toString());

                        SubmitObjectsRequest sor = (SubmitObjectsRequest) XMLUtils.unmarshal(baos.toString(), oasis.names.tc.ebxml_regrep.xsd.lcm._3.ObjectFactory.class);
                        prsr.setSubmitObjectsRequest(sor);
                        docId = getDocId(sor);

                    } else if (matchName(zname, subsetDirspec, XDM_FILENAME_DATA)) {

                        InputStream in = zipFile.getInputStream(zipEntry);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        int bytesRead = 0;
                        byte[] buffer = new byte[2048];
                        while ((bytesRead = in.read(buffer)) != -1) {
                            baos.write(buffer, 0, bytesRead);
                        }
                        in.close();
                        LOGGER.trace("xml data " + baos.toString());
                        List<Document> docs = prsr.getDocument();
                        Document pdoc = new Document();

                        DataSource source = new ByteArrayDataSource(baos.toByteArray(), MimeType.APPLICATION_XML.getType() + "; charset=UTF-8");
                        DataHandler dhnew = new DataHandler(source);
                        pdoc.setValue(dhnew);
                        pdoc.setId(docId);
                        docs.add(pdoc);
                    }
                }
                
                ((Document)prsr.getDocument().get(0)).setId(zname);
            }
        } finally {
            if (zipFile != null) {
                zipFile.close();
            }
        }

        return prsr;
    }

    /**
     * Get the document ID from a SubmitObjectsRequest object.
     * 
     * @param sor
     *            The SubmitObjectsRequest object from which to retrieve the
     *            document ID.
     * @return a document ID.
     */
    protected static String getDocId(SubmitObjectsRequest sor) {
        String ret = null;
        RegistryObjectListType rol = sor.getRegistryObjectList();
        List<JAXBElement<? extends IdentifiableType>> extensible = rol.getIdentifiable();
        
        for (JAXBElement<? extends IdentifiableType> elem : extensible) {
            String type = elem.getDeclaredType().getName();
            Object value = elem.getValue();
            
            if (StringUtils.equals(type, "oasis.names.tc.ebxml_regrep.xsd.rim._3.ExtrinsicObjectType")) {
                ret = getDocId((ExtrinsicObjectType) value);
            }
            
            ///CLOVER:OFF
            if (LOGGER.isTraceEnabled())
                LOGGER.trace(type + " " + value.toString());
            ///CLOVER:ON
        }
        
        return ret;
    }

    /**
     * Get the document ID from an EntrinsicObjectType object.
     * 
     * @param eot
     *            The EntrinsicObjectType object from which to retrieve the
     *            document ID.
     * @return a document ID.
     */
    protected static String getDocId(ExtrinsicObjectType eot) {
        String ret = null;
        
        List<ExternalIdentifierType> eits = eot.getExternalIdentifier();
        
        for (ExternalIdentifierType eit : eits) {
            if (eit.getIdentificationScheme().equals("urn:uuid:2e82c1f6-a085-4c72-9da3-8640a32e42ab")) {
                ret = eit.getValue();
            }
        }
        
        return ret;
    }

    /**
     * Determine whether a filename matches the subset directory and file name.
     * 
     * @param zname
     *            The name to compare.
     * @param subsetDirspec
     *            The subset directory name.
     * @param subsetFilespec
     *            The subset file name.
     * @return true if the names match, false otherwise.
     */
    static boolean matchName(String zname, String subsetDirspec, String subsetFilespec) {
        boolean ret = false;

        String zipFilespec = subsetDirspec + "\\" + subsetFilespec.replace('/', '\\');
        ret = zname.equals(zipFilespec);
        if (!ret) {
            zipFilespec = zipFilespec.replace('\\', '/');
            ret = zname.equals(zipFilespec);
        }
        return ret;
    }

    /**
     * Given a full ZipEntry filespec, extracts the name of the folder (if
     * present) under the IHE_XDM root specified by IHE XDM.
     * 
     * @param zipEntryName
     *            The ZIP entry name.
     * @return the name of the folder.
     */
    protected static String getSubmissionSetDirspec(String zipEntryName) {
        String result = null;
        if (zipEntryName != null) {
            String[] components = zipEntryName.split("\\\\");
            result = components[0];
        }
        return result;
    }

    /**
     * Create a File object from the given DataHandler object.
     * 
     * @param dh
     *            The DataHandler object.
     * @return a File object created from the DataHandler object.
     * @throws Exception
     */
    protected File fileFromDataHandler(DataHandler dh) throws Exception {
        File f = null;
        OutputStream out = null;      
        InputStream inputStream = null;
        
        final String fileName = "xdmail-" + UUID.randomUUID().toString() + ".zip";

        try {
            f = new File(fileName);
            inputStream = dh.getInputStream();
            out = new FileOutputStream(f);
            byte buf[] = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("File not found - " + fileName, e);
            throw e;
        } catch (IOException e) {
            LOGGER.error("Exception thrown while trying to read file from DataHandler object", e);
            throw e;
        } finally {
            if (inputStream != null)
                inputStream.close();
            if (out != null)
                out.close();
        }
        
        LOGGER.info("Created temporary work file " + f.getAbsolutePath());
        
        return f;
    }
}
