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

package rd.slipstream.hazy.farcache.config;

import java.util.List;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;

import rd.jsonmapper.JSON;

/**
 * @author indroneel
 *
 */

public class ClusterConfig {

	@JSON("member-addresses")
	private List<String> members;

	@JSON("connection-timeout")
	private int timeout;

	public final void populate(Config cfg) {
		NetworkConfig netcfg = cfg.getNetworkConfig();
		netcfg.getJoin().getMulticastConfig().setEnabled(false);
		if(members != null && !members.isEmpty()) {
			TcpIpConfig tcpcfg = netcfg.getJoin().getTcpIpConfig();
			tcpcfg.setEnabled(true);
			for(String maddr : members) {
				tcpcfg.addMember(maddr);
			}
			tcpcfg.setConnectionTimeoutSeconds(timeout);
		}
	}
}
