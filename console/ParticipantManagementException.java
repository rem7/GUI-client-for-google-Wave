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
* Thrown when there is a problem with managing participants
*
* For example: When attempting to remove a participant from a wave and the
* participant is not participating in the wave
*/
public class ParticipantManagementException extends Exception{
	public ParticipantManagementException(String msg) {
		super(msg);
	}
}