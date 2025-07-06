package com.example.springai.chapter2;

import lombok.AllArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@AllArgsConstructor
public class ActorFilms2 {
    private String actor;
    private List<Object> movies;
}
