/**
 * Copyright 2009, Acknack Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


package org.waveprotocol.wave.examples.fedone.waveclient.console;
/**
* Contains a single inbox entry. Entry includes wave id, wave digest and read status
*/
public class InboxElement {
	
	private String id;
	private String digest;
	private boolean read;
	
	/**
	* Constructor to create a single element
	* @param String containing message hash value
	* @param String containing message digest
	* @param boolean indicating read status. True if wave read, false if unread
	*/
	public InboxElement(String id, String digest, boolean read) {
		this.id = id;
		this.digest = digest;
		this.read = read;
	}
	
	/**
	* Returns the wave id
	*
	* @return string containing id of wave
	*/
	public String getId() {
		return id;
	}
	
	/**
	* Returns the wave digest
	*
	* @return String containing the message digest
	*/
	public String getDigest() {
		return digest;
	}
	
	/**
	*Returns the read status
	*
	* @return true if message has been read, false if not
	*/
	public boolean getRead() {
		return read;
	}
}