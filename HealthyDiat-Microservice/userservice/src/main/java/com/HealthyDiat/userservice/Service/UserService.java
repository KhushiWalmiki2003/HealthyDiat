package com.HealthyDiat.userservice.Service;
import com.HealthyDiat.userservice.Entity.User;
import com.HealthyDiat.userservice.Repository.UserRepository;
import com.HealthyDiat.userservice.dto.RegisterRequest;
import com.HealthyDiat.userservice.dto.UserResponse;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public @Nullable UserResponse register(@Valid RegisterRequest request)
    {
        if(userRepository.existsByEmail(request.getEmail())){
            throw  new RuntimeException("Email already exist.");
        }
        User user=new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());

        User saveduser=userRepository.save(user);
        UserResponse userResponse=new UserResponse();
        userResponse.setId((saveduser.getId()));
        userResponse.setPassword(saveduser.getPassword());
        userResponse.setEmail(saveduser.getEmail());
        userResponse.setFirstname(saveduser.getFirstname());
        userResponse.setLastname(saveduser.getLastname());
        userResponse.setCreatedAt(saveduser.getCreatedAt());
        userResponse.setUpdatedAt(saveduser.getUpdatedAt());

        return userResponse;
    }

    public UserResponse getUserProfile(String userId){
        User user=userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("User Not Found"));

        UserResponse userResponse= new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setPassword(user.getPassword());
        userResponse.setEmail(user.getEmail());
        userResponse.setFirstname(user.getFirstname());
        userResponse.setLastname(user.getLastname());
        userResponse.setCreatedAt(user.getCreatedAt());
        userResponse.setUpdatedAt(user.getUpdatedAt());

        return  userResponse;
    }
}
