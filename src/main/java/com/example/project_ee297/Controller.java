package com.example.project_ee297;

import com.example.project_ee297.Dao.UserRepository;
import com.example.project_ee297.util.WebFaceCompare;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;

@RequestMapping("/users")
@RestController
@EnableWebSecurity
public class Controller {
    @Autowired
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests(authorize -> authorize
//                        .antMatchers("/users/register", "/users/login").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf().disable();
    }

    private UserRepository userRepository;
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        String username = user.getUsername();
        String password = user.getPassword();
        // check if user already exists
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        // create new user
        User newUser=new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        userRepository.save(newUser);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        String username = user.getUsername();
        String password = user.getPassword();
        // check if user exists
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (!existingUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // check if password matches
        if (!existingUser.get().getPassword().equals(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // login successful, set authentication in SecurityContextHolder
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, password);
        SecurityContextHolder.getContext().setAuthentication(auth);
        return ResponseEntity.ok().build();
    }


     String filePath="C:\\Users\\Lenovo\\Pictures\\Screenshots\\savedImage.jpg";
  @PostMapping("/saveImageInJPG")
    public String handleFileUpload(@RequestParam("file") MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        byte[] fileContent = file.getBytes();
       // File newFile = new File(getCurrentUser().getPhotoPath());
      File newFile = new File(filePath );
        Files.write(newFile.toPath(), fileContent);
        return "File uploaded successfully!";
    }

    @SneakyThrows
    @RequestMapping(value = "/saveImage", method = RequestMethod.POST)
    @ResponseBody
    public String saveImage(@RequestBody  String base64Image) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = null;
        try {
            root = mapper.readTree(base64Image);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        String ImageBase64Code = Objects.requireNonNull(root).get("base64Image").asText();
        byte[] imageByte = Base64.decodeBase64(ImageBase64Code);
        File imageFile = new File(filePath );
        // File imageFile = new File(getCurrentUser().getPhotoPath());
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(imageFile))) {
            outputStream.write(imageByte);
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to save image!";
        }
        return "save image successfully";
    }

    @SneakyThrows
    @RequestMapping(value = "/compare-images", method = RequestMethod.POST)
    @ResponseBody
    public String compareFace(@RequestBody  String base64Image) {
        String imagePath = "C:\\Users\\Lenovo\\Pictures\\Screenshots\\currentImage.jpg";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = null;
        try {
            root = mapper.readTree(base64Image);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        String ImageBase64Code = Objects.requireNonNull(root).get("base64Image").asText();
        WebFaceCompare webFaceCompare=new WebFaceCompare("https://api.xf-yun.com/v1/private/s67c9c78c","c42a8fc5","NDRkOGEwYWQwMzkwYTM2OTc2M2RmODYx", "1389aa5d53e53ae7578609062dffe3b9",imagePath,filePath,"s67c9c78c");
        byte[] imageBytes = Base64.decodeBase64(ImageBase64Code);
        File imageFile = new File(imagePath);
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(imageFile))) {
            outputStream.write(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to save current image!";
        }
        return webFaceCompare.similarity(imagePath,filePath);
        //return   webFaceCompare.similarity(imagePath,getCurrentUser().getPhotoPath());

    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String currentPrincipalName = authentication.getName();
            Optional<User> currentUser = userRepository.findByUsername(currentPrincipalName);
            if (currentUser.isPresent()) {
                return currentUser.get();
            }
        }
        throw new RuntimeException("Failed to get current user!");

    }

}