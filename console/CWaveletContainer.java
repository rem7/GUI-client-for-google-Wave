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

import java.util.ArrayList;
/**
* Allows {@link WaveConnector} to construct a {@link CWavelet} under certain conditions. See {@link WaveConnector}.getWaveBody()
* for full description of use
*/
public class CWaveletContainer {
	
	public CWavelet cwavelet;
	private String author;
	private String text;
	
	/**
	* Constructor resests author and text field when called
	*/
	public CWaveletContainer() {
		author = null;
		text = null;
	}
	
	
	
	/**
	* Sets the author. When author and text are both not null changes are placed in a {@link CWavelet}
	*/
	public void setAuthor(String author) {
		this.author = author;
		pushToWavelet();
	}
	
	/**
	* Sets the content of the Wavelet. When author and text are both not null changes are placed in a {@link CWavelet}
	*/
	public void setText(String text) {
		this.text = text;
		pushToWavelet();
	}
	
	/**
	* Returns the wavelet. Care should be taken to only call this when author and text have been added.
	*
	* @return {@link CWavelet} containing wavelet
	*/
	public CWavelet getWavelet() {
		CWavelet temp = cwavelet;
		cwavelet = null;
		return temp;
	}
	
	
	//Once a author and content has been added, create a wavelet and reset all the content that has been added thus far
	private void pushToWavelet() {
		if(author != null && text != null) {
			cwavelet = new CWavelet(author, text);
			author = null;
			text = null;
		}
	}
}