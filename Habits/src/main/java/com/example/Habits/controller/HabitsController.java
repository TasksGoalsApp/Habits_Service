package com.example.Habits.controller;

import com.example.Habits.business.ICreateHabit;
import com.example.Habits.business.IDeleteHabit;
import com.example.Habits.business.IGetAllHabitsByUser;
import com.example.Habits.business.IUpdateHabit;
import com.example.Habits.domain.Request.CreateHabitRequest;
import com.example.Habits.domain.Request.UpdateHabitRequest;
import com.example.Habits.domain.Response.CreateHabitResponse;
import com.example.Habits.domain.Response.UpdateHabitResponse;
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

    private IDeleteHabit deleteHabit;
    private IGetAllHabitsByUser getAllHabitsByUser;
    private IUpdateHabit updateHabit;

    @PostMapping("/create")
    public ResponseEntity<CreateHabitResponse> createHabit(@RequestBody @Valid CreateHabitRequest createHabitRequest){

        CreateHabitResponse response = createHabit.createHabit(createHabitRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @PutMapping
    public ResponseEntity<UpdateHabitResponse> updateHabit(@RequestBody @Valid UpdateHabitRequest updateHabitRequest){
        UpdateHabitResponse response = updateHabit.updateHabit(updateHabitRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteHabit(){
        //To Do Finish it
        return ResponseEntity.noContent().build();
    }


}
