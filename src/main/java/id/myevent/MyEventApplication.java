package id.myevent;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


/** Main Class. */
@SpringBootApplication
public class MyEventApplication {

  public static void main(String[] args) {
    SpringApplication.run(MyEventApplication.class, args);
  }
}
