/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.UUID;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test; 
import org.junit.runner.RunWith;
import org.pieShare.pieDrive.adapter.exceptions.AdaptorException;
import org.pieShare.pieDrive.adapter.model.PieDriveFile;
import org.pieShare.pieDrive.adapter.s3.S3Adapter;
import org.pieShare.pieDrive.adapter.s3.configuration.S3AdapterConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = S3AdapterConfig.class)
public class S3AdapterTest {
	
	@Autowired
	private S3Adapter s3Client;
	private PieDriveFile file;
	private File testFile;
	byte[] content = "This is a test content!!!!!!!!!!".getBytes();

	@Before
    public void init() {
		UUID uid = UUID.randomUUID();
        testFile = new File(uid.toString());

        if (testFile.exists()) {
            testFile.delete();
        }

        FileOutputStream ff;
        try {
            ff = new FileOutputStream(testFile);
            ff.write(content);
            ff.close();
        } catch (FileNotFoundException ex) {
            Assert.fail(ex.getMessage());
        } catch (IOException ex) {
            Assert.fail(ex.getMessage());
        }

        file = new PieDriveFile();
		file.setSize(content.length);
        file.setUuid(uid.toString());
    }
	
	@After
	public void tearDown(){
		testFile.delete();
	}
	
	private void upload(){
		FileInputStream st = null;
        try {
            st = new FileInputStream(testFile);
        } catch (FileNotFoundException ex) {
            Assert.fail();
        }
		
		try{
		s3Client.upload(file, st);
		}catch (AdaptorException ae){}
		
		boolean found = s3Client.find(file);
		
		Assert.assertTrue(found);
	}
	
	private void download(){
		 File donwloadedFile = new File("downloaded" + file.getUuid());

        try {
			try{
            s3Client.download(file, new FileOutputStream(donwloadedFile));
			}catch (AdaptorException ae){}
        } catch (FileNotFoundException ex) {
            Assert.fail();
        }
		
		byte[] data1 = null;
        try {
            data1 = Files.readAllBytes(donwloadedFile.toPath());
        } catch (IOException ex) {
            Assert.fail();
        }

        Arrays.equals(content, data1);
        
        donwloadedFile.delete();
	}
	
	private void delete(){
		boolean fileFound = false;
		fileFound = s3Client.find(file);
		Assert.assertTrue(fileFound);
		
		try{
		s3Client.delete(file);
		}catch (AdaptorException ae){}
		
		fileFound = s3Client.find(file);
		Assert.assertFalse(fileFound);
	}
	
	@Test
	public void upDownDelTest(){
		upload();
		download();
		delete();
	}
	
	//@Test
	public void bigUpTest(){
		uploadBig();
		//delete();
	}

	private void uploadBig() {
		UUID uid = UUID.randomUUID();
        testFile = new File(uid.toString());

        if (testFile.exists()) {
            testFile.delete();
        }
		
        FileOutputStream ff;
        try {
            ff = new FileOutputStream(testFile);
			for (int i = 0; i < 3125000; i++) {
				ff.write(content);
			}
            ff.close();
        } catch (FileNotFoundException ex) {
            Assert.fail(ex.getMessage());
        } catch (IOException ex) {
            Assert.fail(ex.getMessage());
        }

        file = new PieDriveFile();

		file.setSize(content.length*3125000);
		
        file.setUuid(uid.toString());
		
		upload();
	}
}
