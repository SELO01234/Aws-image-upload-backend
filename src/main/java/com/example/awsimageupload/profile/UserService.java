package com.example.awsimageupload.profile;

import com.example.awsimageupload.bucket.BucketName;
import com.example.awsimageupload.filestore.FileStore;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.http.entity.ContentType;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FileStore fileStore;

    @CachePut(value = "users")
    public List<UserProfile> getUsers() {
        return userRepository.findAll();
    }

    @CacheEvict(value = "users", allEntries = true)
    public boolean addUser(UserProfile user) {
        try{
            userRepository.save(user);
            return true;
        }
        catch (Exception exception){
            return false;
        }
    }

    @CachePut(value = "users")
    @Transactional
    public boolean updateUser(UUID userProfileId, UserProfile user) {
        try{
            UserProfile user2 = userRepository.findById(userProfileId).orElseThrow();

            user2.setUsername(user.getUsername());
            user2.setUserProfileImageLink(user.getUserProfileImageLink());

            userRepository.save(user2);

            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    public void uploadUserProfileImage(UUID userProfileId, MultipartFile file) {
        if(file.isEmpty()){
            throw new IllegalStateException("Cannot upload empty file [" + file.getSize() + "]");
        }
        if(!Arrays.asList(ContentType.IMAGE_JPEG.getMimeType(), ContentType.IMAGE_PNG.getMimeType(), ContentType.IMAGE_GIF.getMimeType()).contains(file.getContentType())){
            throw new IllegalStateException("File must be an image");
        }
        if(userRepository.existsById(userProfileId)){

            UserProfile user = userRepository.findById(userProfileId).orElseThrow();

            Map<String, String> metadata = new HashMap<>();
            metadata.put("Content-Type", file.getContentType());
            metadata.put("Content-Length",String.valueOf(file.getSize()));

            String path = String.format("%s/%s", BucketName.PROFILE_IMAGE.getBucketName(), userProfileId);
            String filename = String.format("%s-%s", file.getOriginalFilename(), UUID.randomUUID());

            try {
                fileStore.save(path ,filename, Optional.of(metadata), file.getInputStream());
                user.setUserProfileImageLink(filename);
                boolean result = updateUser(userProfileId, user);
                if(result){
                    System.out.println("User updated successfully");
                }
                else{
                    System.out.println("User can not be updated");
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public byte[] downloadUserProfileImage(UUID userProfileId) {
        if(userRepository.existsById(userProfileId)){
            UserProfile user =  userRepository.findById(userProfileId).orElseThrow();
            String path = String.format("%s/%s", BucketName.PROFILE_IMAGE.getBucketName(), userProfileId);

            return fileStore.download(path, user.getUserProfileImageLink());
        }
        else{
            System.out.println("User is not exists");
            return null;
        }
    }
}
