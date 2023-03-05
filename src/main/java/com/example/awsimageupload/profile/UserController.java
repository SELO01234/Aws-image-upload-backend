package com.example.awsimageupload.profile;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user-profile")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users")
    public List<UserProfile> getUsers(){
        return userService.getUsers();
    }

    @PostMapping("/add")
    public ResponseEntity<String> addUser(@RequestBody UserProfile user){
        boolean response = userService.addUser(user);

        if(response){
            return ResponseEntity.ok("User has been added successfully");
        }
        else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/update/{userProfileId}")
    public ResponseEntity<String> updateUser(@PathVariable("userProfileId") UUID userProfileId, @RequestBody UserProfile user){
        boolean result = userService.updateUser(userProfileId,user);
        if(result){
            return ResponseEntity.ok("User updated successfully");
        }
        else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping(
            path = "{userProfileId}/image/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public void uploadUserProfileImage(@PathVariable("userProfileId")UUID userProfileId, @RequestParam("file")MultipartFile file) {
        userService.uploadUserProfileImage(userProfileId, file);
    }

    @GetMapping("{userProfileId}/image/download")
    public byte[] downloadUserProfileImage(@PathVariable("userProfileId")UUID userProfileId){
        return userService.downloadUserProfileImage(userProfileId);
    }

}
