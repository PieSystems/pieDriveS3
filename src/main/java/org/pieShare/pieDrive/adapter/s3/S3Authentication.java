/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pieShare.pieDrive.adapter.s3;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import java.io.File;


public class S3Authentication {
	private AmazonS3Client client;
	private final String bucketName = "g4t2aic2015";
	private AWSCredentialsProvider provider;
	private String path;
	
	public S3Authentication(){
		String path = System.getProperty("user.home");
		File pieDrive = new File(path, ".pieDrive");
		File awsFile = new File(pieDrive, "aws");
		this.path = awsFile.getAbsolutePath();

		this.client = new AmazonS3Client();
	}
	
	public boolean authenticate(){
		try{
			provider = new ProfileCredentialsProvider(path, "default");

			this.client = new AmazonS3Client(this.provider);

			client.createBucket(bucketName);
		} catch(AmazonServiceException e){
			return false;
		} catch(AmazonClientException e){
			return false;
		}
		
		return true;
	}
	
	public	AmazonS3Client getClient(){
		return client;
	}
}
