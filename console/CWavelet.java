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
* Contains a single blip. Designed to be extensible so extra functionality
* can be added later such as images etc. Google may provide a similar class
* to this, but this one is designed to be simple and contain as little 
* bloat as possible. This may come under review at a later date and may be
* depriciated in place of a different class.
*/
public class CWavelet {
		
		private String author;
		private String text;
		
		/**
		* Constructor requires an author and the blip contents
		*
		* @param String containing the authors ID
		* @param String containing the content of the blip
		*/
		public CWavelet(String author, String text) {
			this.author = author;
			this.text = text;
		}
		
		/**
		* Returns the author
		*
		* @return the author ID
		*/
		public String getAuthor() {
			return author;
		}
		
		/**
		*Returns the content of the blip
		*
		* @return the content of the blip
		*/
		public String getText() {
			return text;
		}
		
	}