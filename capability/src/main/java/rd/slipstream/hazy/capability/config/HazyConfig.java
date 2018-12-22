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

import rd.jsonmapper.JSON;

/**
 * @author randondiesel
 *
 */

public class HazyConfig {

	@JSON("peer-to-peer")
	private Peer2PeerConfig p2pCfg;

	@JSON("client-server")
	private ClientServerConfig csCfg;

	public final Peer2PeerConfig peer2PeerConfig() {
		return p2pCfg;
	}

	public final ClientServerConfig clientServerConfig() {
		return csCfg;
	}
}
