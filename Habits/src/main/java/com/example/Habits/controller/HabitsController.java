package com.example.Habits.controller;

import com.example.Habits.business.ICreateHabit;
import com.example.Habits.domain.Request.CreateHabitRequest;
import com.example.Habits.domain.Response.CreateHabitResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@AllArgsConstructor
@RestController
@RequestMapping("/habits")
public class HabitsController {
    @Autowired
    private ICreateHabit createHabit;


    @PostMapping("/create")
    public ResponseEntity<CreateHabitResponse> createHabit(@RequestBody @Valid CreateHabitRequest createHabitRequest){

        CreateHabitResponse response = createHabit.createHabit(createHabitRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


}
