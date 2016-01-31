/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pieShare.pieDrive.adapter.s3;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.pieShare.pieDrive.adapter.api.Adaptor;
import org.pieShare.pieDrive.adapter.exceptions.AdaptorException;
import org.pieShare.pieDrive.adapter.model.PieDriveFile;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;
import org.springframework.beans.factory.annotation.Autowired;


public class S3Adapter implements Adaptor{
    private AmazonS3Client s3client;
	private final String bucketName = "g4t2aic2015";
	@Autowired
	private S3Authentication s3Auth;
	
	public S3Adapter(){
		loadConf();
	}
	
	public void setS3Authentication(S3Authentication s3Auth){
		this.s3Auth = s3Auth;
	}
	
	private void loadConf(){
		
	}

	@Override
	public void delete(PieDriveFile file) throws AdaptorException {
		try{
			s3Auth.getClient().deleteObject(new DeleteObjectRequest(bucketName, file.getUuid()));
			PieLogger.trace(S3Adapter.class, "{} deleted", file.getUuid());
		} catch (AmazonServiceException ase) {
			throw new AdaptorException(ase);
		} catch (AmazonClientException ace) {
			throw new AdaptorException(ace);
        }
	}

	@Override
	public synchronized void upload(PieDriveFile file, InputStream stream) throws AdaptorException {
		try{
			ObjectMetadata meta = new ObjectMetadata();
			meta.setContentLength(file.getSize());
			PutObjectRequest req = new PutObjectRequest(bucketName, file.getUuid(), stream, meta);
			//req.getRequestClientOptions().setReadLimit(64);
			s3Auth.getClient().putObject(req);
			//Thread.sleep(2000);
			PieLogger.trace(S3Adapter.class, "{} uploaded", file.getUuid());
		} catch (AmazonServiceException ase) {
			throw new AdaptorException(ase);
		} catch (AmazonClientException ace) {
			throw new AdaptorException(ace);
        } catch (Exception e){}
	}

	@Override
	public void download(PieDriveFile file, OutputStream stream) throws AdaptorException {
		byte[] buf = new byte[1024];
		int count = 0;
		
		S3Object object = s3Auth.getClient().getObject(new GetObjectRequest(bucketName, file.getUuid()));
		InputStream objectData = object.getObjectContent();
		
		try{
			while((count = objectData.read(buf)) != -1) {
			   if(Thread.interrupted() )
			   {
				   throw new AdaptorException("Download interrupted.");
			   }

			   stream.write(buf, 0, count);
			}

			stream.close();
			objectData.close();
			PieLogger.trace(S3Adapter.class, "{} downloaded", file.getUuid());
			
		} catch(IOException e){
			throw new AdaptorException(e);
		} catch (AmazonServiceException ase) {
			throw new AdaptorException(ase);
		} catch (AmazonClientException ace) {
			throw new AdaptorException(ace);
        }
	}
	
	public boolean find(PieDriveFile file){
		ObjectListing listing = s3Auth.getClient().listObjects(bucketName);
		boolean ret = false;
		
		for (S3ObjectSummary objectSummary : listing.getObjectSummaries()) {
			ret = objectSummary.getKey().equals(file.getUuid());
			if(ret == true){
				break;
			}
		}
		
		return ret;
	}
}
