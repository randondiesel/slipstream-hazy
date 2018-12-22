/*
 * Copyright (c) The original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package rd.slipstream.hazy.capability.config;

import java.util.List;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.client.config.SocketOptions;

import rd.jsonmapper.JSON;

/**
 * @author indroneel
 *
 */

public class ClientServerConfig {

	@JSON("member-addresses")
	private List<String> members;

	@JSON("connection-timeout")
	private int timeout;

	@JSON("connection-attempt-period")
	private int connAtmptPrd;

	@JSON("connection-attempt-limit")
	private int connAtmptLmt;

	public ClientServerConfig() {
		timeout = 30000;
		connAtmptPrd = 5000;
	}

	public final void populate(ClientConfig cfg) {
		ClientNetworkConfig nwcfg = cfg.getNetworkConfig();
		if(members != null) {
			nwcfg.addAddress(members.toArray(new String[members.size()]));
		}
		nwcfg.setSmartRouting(true);
		nwcfg.setConnectionTimeout(timeout);
		nwcfg.setRedoOperation(true);
		nwcfg.setConnectionAttemptPeriod(connAtmptPrd);
		nwcfg.setConnectionAttemptLimit(connAtmptLmt);

		SocketOptions sockOpts = nwcfg.getSocketOptions();
		//Provide capability to set this values from external configuration
		sockOpts.setBufferSize(32);
		sockOpts.setKeepAlive(true);
		sockOpts.setTcpNoDelay(true);
		sockOpts.setReuseAddress(true);
		sockOpts.setLingerSeconds(3);
	}
}
