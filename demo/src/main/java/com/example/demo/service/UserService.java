package com.example.demo.service;

import com.example.demo.dto.UserDto;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto createUser(UserDto userDto) {
        User user = new User(null, userDto.getName(), userDto.getEmail());
        user = userRepository.save(user);
        return toDto(user);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
        return toDto(user);
    }

    public UserDto updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user = userRepository.save(user);
        return toDto(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id " + id);
        }
        userRepository.deleteById(id);
    }

    private UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }
}
