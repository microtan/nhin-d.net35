﻿/* 
 Copyright (c) 2010, NHIN Direct Project
 All rights reserved.

 Authors:
    Umesh Madan     umeshma@microsoft.com
  
Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
Neither the name of the The NHIN Direct Project (nhindirect.org). nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
*/
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;
using NHINDirect.Agent;
using NHINDirect.Config.Store;
using NHINDirect.Config.Client.DomainManager;

namespace NHINDirect.SmtpAgent
{
    /// <summary>
    /// Facade around the Config System
    /// We'll bake in Caching and other enhancements here..
    /// </summary>
    internal class ConfigService
    {
        SmtpAgentSettings m_settings;
                
        internal ConfigService(SmtpAgentSettings settings)
        {
            m_settings = settings;
        }

        internal Domain[] GetDomains(string[] domainNames)
        {
            Debug.Assert(m_settings.HasDomainManagerService);

            using(DomainManagerClient client = m_settings.DomainManagerService.CreateDomainManagerClient())
            {
                return client.GetDomains(domainNames, EntityStatus.Enabled);
            }
        }
        
        internal Address GetAddress(NHINDAddress address)
        {
            Debug.Assert(m_settings.HasAddressManager);
            
            using(AddressManagerClient client = this.CreateAddressManagerClient())
            {
                return client.GetAddress(address, EntityStatus.Enabled);
            }                
        }
        
        internal Address[] GetAddresses(NHINDAddressCollection addresses)
        {
            Debug.Assert(m_settings.HasAddressManager);

            string[] emailAddresses = (
                from address in addresses
                select address.Address
            ).ToArray();

            using(AddressManagerClient client = this.CreateAddressManagerClient())
            {
                return client.GetAddresses(emailAddresses, EntityStatus.Enabled);
            }
        }

        internal Address[] GetAddresses(long[] addressIDs)
        {
            Debug.Assert(m_settings.HasAddressManager);

            using(AddressManagerClient client = this.CreateAddressManagerClient())
            {
                return client.GetAddressesByID(addressIDs, EntityStatus.Enabled);
            }
        }
        
        AddressManagerClient CreateAddressManagerClient()
        {
            return m_settings.AddressManager.CreateAddressManagerClient();
        }
    }
}