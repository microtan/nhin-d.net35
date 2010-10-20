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

package org.nhind.xdm.impl;

import java.io.File;
import java.util.Properties;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang.StringUtils;
import org.nhind.xdm.MailClient;
import org.nhindirect.xd.common.DirectDocument;
import org.nhindirect.xd.common.DirectMessage;
import org.nhindirect.xd.transform.DocumentXdmTransformer;
import org.nhindirect.xd.transform.impl.DocumentXdmTransformerImpl;

/**
 * This class handles the packaging and sending of XDM data over SMTP.
 * 
 * @author vlewis
 */
public class SmtpMailClient implements MailClient
{
    final static int BUFFER = 2048;
    
    private MimeMessage mmessage;
    private Multipart mailBody;
    private MimeBodyPart mainBody;
    private MimeBodyPart mimeAttach;
   
    private String hostname = null;
    private String username = null;
    private String password = null;

    private DocumentXdmTransformer transformer = new DocumentXdmTransformerImpl();
    
    public SmtpMailClient(String hostname, String username, String password)
    {
        if (StringUtils.isBlank(hostname) || StringUtils.isBlank(username) || StringUtils.isBlank(password))
            throw new IllegalArgumentException("Hostname, username, and password must be provided.");
        
        this.hostname = hostname;
        this.username = username;
        this.password = password;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.nhind.xdm.MailClient#postMail(org.nhindirect.xd.common.DirectMessage, java.lang.String)
     */
    public void mail(DirectMessage message, String messageId, String suffix) throws MessagingException
    {
        boolean debug = false;
        java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

        // Set the host SMTP address
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", hostname);
        props.put("mail.smtp.auth", "true");

        Authenticator auth = new SMTPAuthenticator();
        Session session = Session.getInstance(props, auth);

        session.setDebug(debug);

        InternetAddress addressFrom = new InternetAddress(message.getSender());

        InternetAddress[] addressTo = new InternetAddress[message.getReceivers().size()];
        int i = 0;
        for (String recipient : message.getReceivers())
        {
            addressTo[i++] = new InternetAddress(recipient);
        }

        // Build message object
        mmessage = new MimeMessage(session);
        mmessage.setFrom(addressFrom);
        mmessage.setRecipients(Message.RecipientType.TO, addressTo);
        mmessage.setSubject(message.getSubject());

        mailBody = new MimeMultipart();

        mainBody = new MimeBodyPart();
        mainBody.setDataHandler(new DataHandler(message.getBody(), "text/plain"));
        mailBody.addBodyPart(mainBody);

        mimeAttach = new MimeBodyPart();

        try
        {
            for (DirectDocument document : message.getDocuments())
            {
                File zipout = transformer.transform(document, suffix, messageId);
                mimeAttach.attachFile(zipout);
            }

        }
        catch (Exception x)
        {
            x.printStackTrace();
        }

        mailBody.addBodyPart(mimeAttach);

        mmessage.setContent(mailBody);
        Transport.send(mmessage);
    }

    /**
     * SimpleAuthenticator is used to do simple authentication when the SMTP
     * server requires it.
     */
    private class SMTPAuthenticator extends javax.mail.Authenticator
    {

        /*
         * (non-Javadoc)
         * 
         * @see javax.mail.Authenticator#getPasswordAuthentication()
         */
        @Override
        public PasswordAuthentication getPasswordAuthentication()
        {
            return new PasswordAuthentication(username, password);
        }
    }

}