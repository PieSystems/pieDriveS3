/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pieShare.pieDrive.adapter.s3;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.pieShare.pieDrive.adapter.api.Adaptor;
import org.pieShare.pieDrive.adapter.exceptions.AdaptorException;
import org.pieShare.pieDrive.adapter.model.PieDriveFile;


public class S3Adapter implements Adaptor{
    private AmazonS3Client s3client = new AmazonS3Client();
	private String bucketName = "g4t2";

	@Override
	public void delete(PieDriveFile file) throws AdaptorException {
		try{
			s3client.deleteObject(new DeleteObjectRequest(bucketName, file.getUuid()));
		} catch (AmazonServiceException ase) {
			throw new AdaptorException(ase);
		} catch (AmazonClientException ace) {
			throw new AdaptorException(ace);
        }
	}

	@Override
	public void upload(PieDriveFile file, InputStream stream) throws AdaptorException {
		try{
		s3client.putObject(new PutObjectRequest(bucketName, file.getUuid(), stream, null));
		} catch (AmazonServiceException ase) {
			throw new AdaptorException(ase);
		} catch (AmazonClientException ace) {
			throw new AdaptorException(ace);
        }
	}

	@Override
	public void download(PieDriveFile file, OutputStream stream) throws AdaptorException {
		byte[] buf = new byte[1024];
		int count = 0;
		
		S3Object object = s3client.getObject(new GetObjectRequest(bucketName, file.getUuid()));
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
			
		} catch(IOException e){
			throw new AdaptorException(e);
		} catch (AmazonServiceException ase) {
			throw new AdaptorException(ase);
		} catch (AmazonClientException ace) {
			throw new AdaptorException(ace);
        }
	}
	
	public boolean find(PieDriveFile file){
		ObjectListing listing = s3client.listObjects(bucketName);
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
