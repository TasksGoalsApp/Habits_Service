package com.example.Habits.controller;

import com.example.Habits.business.ICreateHabit;
import com.example.Habits.business.IDeleteHabit;
import com.example.Habits.business.IGetAllHabitsByUser;
import com.example.Habits.business.IUpdateHabit;
import com.example.Habits.domain.Request.CreateHabitRequest;
import com.example.Habits.domain.Request.UpdateHabitRequest;
import com.example.Habits.domain.Response.CreateHabitResponse;
import com.example.Habits.domain.Response.GetAllHabitsByUserResponse;
import com.example.Habits.domain.Response.UpdateHabitResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@AllArgsConstructor
@RestController
@RequestMapping("/habits")
public class HabitsController {

    private final ICreateHabit createHabit;

    private final IDeleteHabit deleteHabit;
    private final IGetAllHabitsByUser getAllHabitsByUser;
    private final IUpdateHabit updateHabit;

    @PostMapping()
    public ResponseEntity<CreateHabitResponse> createHabit(@RequestBody @Valid CreateHabitRequest createHabitRequest, @AuthenticationPrincipal Jwt jwt){
        Long userId = jwt.getClaim("id");
        CreateHabitResponse response = createHabit.createHabit(createHabitRequest, userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @PutMapping("{habitId}")
    public ResponseEntity<UpdateHabitResponse> updateHabit(@RequestBody @Valid UpdateHabitRequest updateHabitRequest, @PathVariable Long habitId, @AuthenticationPrincipal Jwt jwt ){
       Long userId = jwt.getClaim("id");
       UpdateHabitResponse response = updateHabit.updateHabit(updateHabitRequest, habitId, userId);
       return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("{habitId}")
    public ResponseEntity<Void> deleteHabit(@PathVariable Long habitId, @AuthenticationPrincipal Jwt jwt){
        Long userId = jwt.getClaim("id");
        deleteHabit.deleteHabit(habitId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<GetAllHabitsByUserResponse> getAllHabitsByUser(@AuthenticationPrincipal Jwt jwt){
        Long userId = jwt.getClaim("id");
        GetAllHabitsByUserResponse response = getAllHabitsByUser.getAllHabits(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


}
