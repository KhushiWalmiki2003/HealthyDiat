package com.example.userservice.Service;

import com.example.userservice.Model.User;
import com.example.userservice.dto.RegisterRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final String keycloakId="3fcf8711-e319-4461-868c-b0fd3441585c";
    public UserResponse register( RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            User existingUser = userRepository.findByEmail(request.getEmail());
            UserResponse userResponse = new UserResponse();
            userResponse.setId(existingUser.getId());
            userResponse.setKeycloakId(keycloakId);
            userResponse.setPassword(existingUser.getPassword());
            userResponse.setEmail(existingUser.getEmail());
            userResponse.setFirstname(existingUser.getFirstname());
            userResponse.setLastname(existingUser.getLastname());
            userResponse.setCreatedAt(existingUser.getCreatedAt());
            userResponse.setUpdatedAt(existingUser.getUpdatedAt());
            return userResponse;
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setKeycloakId(keycloakId);
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());

        User saveUser = userRepository.save(user);
        UserResponse userResponse = new UserResponse();
        userResponse.setKeycloakId(keycloakId);
        userResponse.setId(saveUser.getId());
        userResponse.setPassword(saveUser.getPassword());
        userResponse.setEmail(saveUser.getEmail());
        userResponse.setFirstname(saveUser.getFirstname());
        userResponse.setLastname(saveUser.getLastname());
        userResponse.setCreatedAt(saveUser.getCreatedAt());
        userResponse.setUpdatedAt(saveUser.getUpdatedAt());

        return userResponse;
    }

    public UserResponse getUserProfile(String userId) {
      User user=userRepository.findById(userId)
              .orElseThrow(()->new RuntimeException("User Not Found "));

      UserResponse response=new UserResponse();
      response.setId(user.getId());
      response.setPassword(user.getPassword());
      response.setEmail(user.getEmail());
      response.setFirstname(user.getFirstname());
      response.setLastname(user.getLastname());
      response.setCreatedAt(user.getCreatedAt());
      response.setUpdatedAt(user.getUpdatedAt());

      return response;

    }

    public Boolean existByUserId(String userId) {
        log.info("Calling User Validation API for userId :{} ",userId);
        return userRepository.existsByKeycloakId(userId);
    }
}