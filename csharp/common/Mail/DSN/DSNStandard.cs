/* 
Copyright (c) 2010, NHIN Direct Project
All rights reserved.

Authors:
   Greg Meyer      gm2552@cerner.com
   Joe Shook       jshook@kryptiq.com
 * 
Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer 
in the documentation and/or other materials provided with the distribution.  Neither the name of the The NHIN Direct Project (nhindirect.org). 
nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS 
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
THE POSSIBILITY OF SUCH DAMAGE.
*/
using System;
using System.Net.Mime;
using Health.Direct.Common.Mime;

namespace Health.Direct.Common.Mail.DSN
{
    /// <summary>
    /// Provides constants and utility functions for working with DSN
    /// </summary>
    /// <remarks>
    /// Delivery Status Notificatione
    /// RFC 3464
    /// http://tools.ietf.org/html/rfc3464
    /// </remarks>
    public class DSNStandard
    {
        /// <summary>
        /// Media type constants
        /// </summary>
        public class MediaType : MailStandard.MediaType
        {
            /// <summary>
            /// Base MIME type for an MDN
            /// </summary>
            public const string ReportMessage = "multipart/report";

            /// <summary>
            /// MIME type with qualifier for a disposition report
            /// </summary>
            public const string DSNReport = ReportMessage + "; report-type=delivery-status";
         
            /// <summary>
            /// The DSN part content type
            /// </summary>
            public static string DSNDeliveryStatus = "message/delivery-status";

            /// <summary>
	        ///  Report parameter value indication a DSN message.
            /// </summary>
            public static string ReportTypeValueDelivery = "delivery-status";

            /// <summary>
            /// MIME report-type
            /// </summary>
            public static string ReportType = "report-type";

        }
        /// <summary>
        /// DSN standard headers
        /// </summary>
        public static class Fields
        {
            /// <summary>
            /// Final recipient
            /// </summary>

            public static string FinalRecipient = "Final-Recipient";

            /// <summary>
            /// DSN Action
            /// </summary>
            public static string Action = "Action";

            /// <summary>
            /// DSN Status
            /// </summary>
            public static string Status = "Status";

            /// <summary>
            /// Name for the reporting MTA
            /// </summary>
            public static string ReportingMTA = "Reporting-MTA";

            /// <summary>
            /// Not part of the DSN standard, but a custom header for the original message id
            /// </summary>
            public static string OriginalMessageID = "X-Original-Message-ID";
        }

        internal const string AddressType_Mail = "rfc822";
        internal const string DsnAction_Failed = "failed";
        internal const string DsnAction_Delayed = "delayed";
        internal const string DsnAction_Delivered = "delivered";
        internal const string DsnAction_Relayed = "relayed";
        internal const string DsnAction_Expanded = "expanded";

        /// <summary>
        /// Enumeration of values for DSN action field
        /// </summary>
        /// <remarks>
        /// rfc 3464 2.3.3
        /// The Action field indicates the action performed by the Reporting-MTA
        /// as a result of its attempt to deliver the message to this recipient
        /// address.  This field MUST be present for each recipient named in the
        /// DSN.
        ///</remarks>
        public enum DSNAction
        {
            /// <summary>
            /// indicates that the message could not be delivered to the recipient. The
            /// Reporting MTA has abandoned any attempts to deliver the message to this
            /// recipient. No further notifications should be expected.
            /// </summary>
            Failed,
            /// <summary>
            /// indicates that the Reporting MTA has so far been unable to deliver or relay
            /// the message, but it will continue to attempt to do so. Additional
            /// notification messages may be issued as the message is further delayed or
            /// successfully delivered, or if delivery attempts are later abandoned.
            /// </summary>
            Delayed,

            /// <summary>
            /// indicates that the message was successfully delivered to the recipient
            /// address specified by the sender, which includes "delivery" to a mailing
            /// list exploder. It does not indicate that the message has been read. This is
            /// a terminal state and no further DSN for this recipient should be expected. 
            /// </summary>
            Delivered,
            /// <summary>
            /// indicates that the message has been relayed or gatewayed into an
            /// environment that does not accept responsibility for generating DSNs upon
            /// successful delivery. This action-value SHOULD NOT be used unless the sender
            /// has requested notification of successful delivery for this recipient.
            /// </summary>
            Relayed,
            /// <summary>
            /// indicates that the message has been successfully delivered to the recipient
            /// address as specified by the sender, and forwarded by the Reporting-MTA
            /// beyond that destination to multiple additional recipient addresses. An
            /// action-value of "expanded" differs from "delivered" in that "expanded" is
            /// not a terminal state. Further "failed" and/or "delayed" notifications may
            /// be provided.
            /// </summary>
            Expanded
        }

        /// <summary>
        /// Enumeration of valid value for MtaNameType fields
        /// </summary>
        public enum MtaNameType
        {
            /// <summary>
            /// rfc 3464 2.1.2 (c) 
            /// For an SMTP server on an Internet host, the MTA name is the domain name of
            /// that host, and the "dns" MTA- name-type is used.
            /// </summary>
            Dns
        }


        /// <summary>
        /// Provides the appropriate Action header value for the <paramref name="type"/>
        /// </summary>
        /// <param name="type">The DSN Action to translate</param>
        /// <returns>A string representation suitable for the Action header value</returns>
        public static string ToString(DSNAction type)
        {
            switch (type)
            {
                default:
                    throw new NotSupportedException();

                case DSNAction.Failed:
                    return DsnAction_Failed;

                case DSNAction.Delayed:
                    return DsnAction_Delayed;

                case DSNAction.Delivered:
                    return DsnAction_Delivered;

                case DSNAction.Relayed:
                    return DsnAction_Relayed;

                case DSNAction.Expanded:
                    return DsnAction_Expanded;

            }


        }

        internal const string MtaNameType_Dns = "dns";

        /// <summary>
        /// Provides the appropriate MTA-name-type value for the <paramref name="type"/>
        /// </summary>
        /// <param name="type">The MTA-name-type to translate</param>
        /// <returns>A string representation suitable for MTA-name-type header value</returns>
        public static string ToString(MtaNameType type)
        {
            switch (type)
            {
                default:
                    throw new NotSupportedException();

                case MtaNameType.Dns:
                    return MtaNameType_Dns;
            }
        }

        /// <summary>
        /// Constants and getters for RFC 3463 Enhanced Mail System Status Codes
        /// </summary>
        public static class DSNStatus
        {
            //
            // status code classes
            //


            ///<summary>
            /// Success
            ///</summary>
            public const int Success = 2;


            ///<summary>
            /// Persistent Transient Failure
            ///</summary>
            public const int Transient = 4;


            ///<summary>
            /// Permanent Failure
            ///</summary>
            public const int Permanent = 5;


            //
            // subjects and details
            //

            ///<summary>
            /// Other or Undefined Status
            ///</summary>
            public const int Undefined = 0;

            /// <summary>
            /// Other undefined status
            /// </summary>
            public static string UNDEFINED_STATUS = "0.0";

            /// <summary>
            /// Delivery time expired
            /// </summary>
	        public static string NETWORK_EXPIRED = "4.7";

            /// <summary>
            /// Delivery time expired for processed MDN
            /// </summary>
            public static string NETWORK_EXPIRED_PROCESSED = "4.71";


            /// <summary>
            /// Delivery time expired for dispatched MDN
            /// </summary>
            public static string NETWORK_EXPIRED_DISPATCHED = "4.72";


            /// <summary>
            /// Build Status Code Structure
            /// </summary>
            /// <remarks>
            /// 
            /// This document defines a new set of status codes to report mail system
            /// conditions.  These status codes are used for media and language
            /// independent status reporting.  They are not intended for system
            /// specific diagnostics.
            ///
            /// The syntax of the new status codes is defined as:
            ///
            /// status-code = class "." subject "." detail
            ///
            /// class = "2"/"4"/"5"
            /// subject = 1*3digit
            ///
            /// detail = 1*3digit
            /// 
            /// </remarks>
            /// /// <param name="type"></param>
            /// <param name="detail"></param>
            /// <returns></returns>
            public static String GetStatus(int type, String detail)
            {
                return type + "." + detail;
            }

            /// <summary>
            /// Build Status Code Structure
            /// </summary>
            /// <param name="type"></param>
            /// <param name="subject"></param>
            /// <param name="detail"></param>
            /// <returns></returns>
            /// <remarks>
            /// This document defines a new set of status codes to report mail system
            /// conditions.  These status codes are used for media and language
            /// independent status reporting.  They are not intended for system
            /// specific diagnostics.
            ///
            /// The syntax of the new status codes is defined as:
            ///
            /// status-code = class "." subject "." detail
            ///
            /// class = "2"/"4"/"5"
            /// subject = 1*3digit
            ///
            /// detail = 1*3digit
            /// </remarks>
            public static String GetStatus(int type, int subject, int detail)
            {
                return type + "." + subject + "." + detail;
            }
        }

        /// <summary>
        /// Tests the <paramref name="entity"/> to see if it is an DSN
        /// </summary>
        /// <remarks>
        /// DSN status is indicated by the appropriate main body <c>Content-Type</c>. The multipart body
        /// will contain the approprieate report-type />
        /// </remarks>
        /// <param name="entity">The entity to test</param>
        /// <returns><c>true</c> if the entity is an DSN, <c>false</c> otherwise</returns>
        public static bool IsReport(MimeEntity entity)
        {
            if (entity == null)
            {
                return false;
            }

            ContentType contentType = entity.ParsedContentType;
            return (contentType.IsMediaType(MediaType.ReportMessage) && contentType.HasParameter(MediaType.ReportType, MediaType.ReportTypeValueDelivery));
        }
    }
}