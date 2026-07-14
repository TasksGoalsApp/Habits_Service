package com.example.Habits.controller;

import com.example.Habits.business.*;
import com.example.Habits.business.HabitCompletion.ICompleteHabit;
import com.example.Habits.business.HabitCompletion.IGetHabitCompletionStatus;
import com.example.Habits.business.HabitCompletion.IGetHabitCompletions;
import com.example.Habits.business.HabitCompletion.IUncompleteHabit;
import com.example.Habits.domain.Request.CreateHabitRequest;
import com.example.Habits.domain.Request.UpdateHabitRequest;
import com.example.Habits.domain.Response.*;
import com.example.Habits.domain.Response.HabitCompletion.CompleteHabitResponse;
import com.example.Habits.domain.Response.HabitCompletion.GetHabitCompletionResponse;
import com.example.Habits.domain.Response.HabitCompletion.HabitCompletionStatusResponse;
import com.example.Habits.security.JwtUserIdExtractor;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/habits")
public class HabitsController {

    private final IGetHabitCompletionStatus getHabitCompletionStatus;
    private final IGetHabitCompletions getHabitCompletions;
    private final IUncompleteHabit uncompleteHabit;
    private final ICompleteHabit completeHabit;
    private final IActivateHabit activateHabit;
    private final IDeactivateHabit deactivateHabit;
    private final JwtUserIdExtractor jwtUserIdExtractor;
    private final ICreateHabit createHabit;
    private final IDeleteHabit deleteHabit;
    private final IGetAllHabitsByUser getAllHabitsByUser;
    private final IUpdateHabit updateHabit;

    @PostMapping()
    public ResponseEntity<CreateHabitResponse> createHabit(@RequestBody @Valid CreateHabitRequest createHabitRequest, @AuthenticationPrincipal Jwt jwt){
        Long userId = jwtUserIdExtractor.extract(jwt);
        CreateHabitResponse response = createHabit.createHabit(createHabitRequest, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PutMapping("/{habitId}")
    public ResponseEntity<UpdateHabitResponse> updateHabit(@RequestBody @Valid UpdateHabitRequest updateHabitRequest, @PathVariable Long habitId, @AuthenticationPrincipal Jwt jwt ){
       Long userId = jwtUserIdExtractor.extract(jwt);
       UpdateHabitResponse response = updateHabit.updateHabit(updateHabitRequest, habitId, userId);
       return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{habitId}")
    public ResponseEntity<Void> deleteHabit(@PathVariable Long habitId, @AuthenticationPrincipal Jwt jwt){
        Long userId = jwtUserIdExtractor.extract(jwt);
        deleteHabit.deleteHabit(habitId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<GetAllHabitsByUserResponse> getAllHabitsByUser(@AuthenticationPrincipal Jwt jwt){
        Long userId = jwtUserIdExtractor.extract(jwt);
        GetAllHabitsByUserResponse response = getAllHabitsByUser.getAllHabits(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{habitId}/deactivate")
    public ResponseEntity<Void> deactivateHabit(@PathVariable Long habitId,@AuthenticationPrincipal Jwt jwt ){
        Long userId = jwtUserIdExtractor.extract(jwt);
        deactivateHabit.deactivateHabit(habitId, userId);
        return ResponseEntity.noContent().build();

    }
    @PatchMapping("/{habitId}/activate")
    public ResponseEntity<Void> activateHabit(@PathVariable Long habitId,@AuthenticationPrincipal Jwt jwt){
        Long userId = jwtUserIdExtractor.extract(jwt);
        activateHabit.activateHabit(habitId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{habitId}/completions")
    public ResponseEntity<CompleteHabitResponse> completeHabit(@PathVariable Long habitId, @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwtUserIdExtractor.extract(jwt);
        CompleteHabitResponse response = completeHabit.completeHabit(habitId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{habitId}/completion-status")
    public ResponseEntity<HabitCompletionStatusResponse> getHabitCompletionStatus(@PathVariable Long habitId, @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwtUserIdExtractor.extract(jwt);
        HabitCompletionStatusResponse response = getHabitCompletionStatus.getStatus(habitId, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{habitId}/completions/current")
    public ResponseEntity<Void> uncompleteHabit(@PathVariable Long habitId, @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwtUserIdExtractor.extract(jwt);
        uncompleteHabit.uncompleteHabit(habitId, userId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{habitId}/completions")
    public ResponseEntity<GetHabitCompletionResponse> getCompletions(@PathVariable Long habitId, @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwtUserIdExtractor.extract(jwt);
        GetHabitCompletionResponse response = getHabitCompletions.getCompletions(habitId, userId);
        return ResponseEntity.ok(response);
    }

}
