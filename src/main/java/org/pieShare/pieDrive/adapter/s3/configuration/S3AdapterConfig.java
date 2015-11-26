/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pieShare.pieDrive.adapter.s3.configuration;

import org.pieShare.pieDrive.adapter.s3.S3Adapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3AdapterConfig {
	
	@Bean
	public S3Adapter s3Adapter(){
		return new S3Adapter();
	}
}
