# *Je*tris - A Game of Tetrominoes

**Table of Contents**

- [Introduction](#introduction)
- [Controls](#controls)
    - [Menu](#menu)
    - [Gameplay](#gameplay)
    - [CLI](#cli)
- [Todos](#todos)
- [Instructions for Grader](#grader)

<a id="introduction" />

This project is an implementation of a game mechanism popularized by Tetris® since 1985. This mechanism consists of a complex set of rules that governs the experience and the outcome of each gameplay. As such, it presents a series of challenges in designing, implementing, and testing the programs over the course of the project.

The Tetris® gameplay (which Jetris implements) has shown beneficial effects to the human brain in a number of research studies. Such benefits range from improved critical thinking and reasoning[[1]](https://doi.org/10.1186%2F1756-0500-2-174), visual pattern recognition[[2]](https://doi.org/10.1126/science.290.5490.350), to the reduction of involuntary flashbacks to traumatic events for people with post-traumatic stress disorder[[3]](https://web.archive.org/web/20201101090805/https://www.nhs.uk/news/mental-health/can-playing-tetris-help-prevent-ptsd/). Thus, this project implements a game mechanism that is not only fun and captivating, but also suitable and beneficial to all kinds of people.

## Controls

### Menu

> `w` - select previous item
>
> `s` - select next item
>
> `space` / `enter` - confirm selection

### Gameplay

> `a` - move left
>
> `d` - move right
>
> `w` - hard drop to the bottom and lock
>
> `s` - soft drop one cell (or hold to drop continuously)
>
> `q` / `k` - rotate counterclockwise
>
> `e` / `l` - rotate clockwise
>
> `space` - hold
>
> `escape` - pause menu

<a id="cli" />

### Command-Line Interface

> `-` - zoom out
>
> `+` - zoom in

<a id="todos" />

## User Stories (Todos)

I want to be able to

- ~~start a new game session~~
- ~~see the playfield~~
- ~~see the queue of upcoming tetrominoes that are shuffled with the 7 bag~~
- ~~add a tetromino to the hold area~~
- ~~place a tetromino down~~
- ~~clear a line~~
- ~~rotate a tetromino~~
- ~~end the game when the game is over~~
- see the score when the game is over
- ~~level up as score increases~~
- ~~speed up the game as level increases~~
- ~~pause the game~~
- ~~save the game to a file once the game is paused~~
- ~~load the game from a file in the starting screen~~

<a id="grader" />

## Instructions for Grader

- Once the application is launched, you may select a start menu item by either clicking on it with your cursor, or navigating to it with the `w` key and the `s` key, then confirming your selection with the `space` key.
- On the start menu, select 'Start', you will start a new game session and see tetromino pieces being added to the playfield. You may interact with the pieces using the [controls](#gameplay) outlined above.
- In the game session, use the `escape` key to summon the pause menu. Select 'Save' and the current game state will be stored in the save file.
- On the pause menu, select 'Main menu' to exit the game and return to the start menu. Select 'Load save' and you will resume the game session that is previously stored in the save file.

## Logging

```text
Wed Nov 29 18:34:26 PST 2023
Game start.
Wed Nov 29 18:34:26 PST 2023
Spawn tetromino S.
Wed Nov 29 18:34:31 PST 2023
Lock tetromino S
Wed Nov 29 18:34:31 PST 2023
Clear 0 lines.
Wed Nov 29 18:34:31 PST 2023
Spawn tetromino T.
Wed Nov 29 18:34:35 PST 2023
Lock tetromino T
Wed Nov 29 18:34:35 PST 2023
Clear 0 lines.
Wed Nov 29 18:34:35 PST 2023
Spawn tetromino L.
Wed Nov 29 18:34:40 PST 2023
Lock tetromino L
Wed Nov 29 18:34:40 PST 2023
Clear 0 lines.
Wed Nov 29 18:34:40 PST 2023
Spawn tetromino O.
Wed Nov 29 18:34:43 PST 2023
Hold tetromino O and spawn tetromino I.
Wed Nov 29 18:34:45 PST 2023
Rotate tetromino I left.
Wed Nov 29 18:34:48 PST 2023
Lock tetromino I
Wed Nov 29 18:34:48 PST 2023
Clear 0 lines.
Wed Nov 29 18:34:48 PST 2023
Spawn tetromino J.
Wed Nov 29 18:34:53 PST 2023
Lock tetromino J
Wed Nov 29 18:34:53 PST 2023
Clear 0 lines.
Wed Nov 29 18:34:53 PST 2023
Spawn tetromino Z.
Wed Nov 29 18:34:54 PST 2023
Hold tetromino Z and spawn tetromino O.
Wed Nov 29 18:34:57 PST 2023
Lock tetromino O
Wed Nov 29 18:34:57 PST 2023
Clear 2 lines.
Wed Nov 29 18:34:57 PST 2023
Spawn tetromino Z.
Wed Nov 29 18:35:01 PST 2023
Game pause.
Wed Nov 29 18:35:09 PST 2023
Game resume.
Wed Nov 29 18:35:12 PST 2023
Lock tetromino Z
Wed Nov 29 18:35:12 PST 2023
Clear 0 lines.
Wed Nov 29 18:35:12 PST 2023
Spawn tetromino S.
Wed Nov 29 18:35:19 PST 2023
Rotate tetromino S left.
Wed Nov 29 18:35:21 PST 2023
Rotate tetromino S right.
Wed Nov 29 18:35:24 PST 2023
Hold tetromino S and spawn tetromino Z.
Wed Nov 29 18:35:27 PST 2023
Lock tetromino Z
Wed Nov 29 18:35:27 PST 2023
Clear 0 lines.
Wed Nov 29 18:35:27 PST 2023
Spawn tetromino I.
Wed Nov 29 18:35:31 PST 2023
Hold tetromino I and spawn tetromino S.
Wed Nov 29 18:35:32 PST 2023
Rotate tetromino S left.
Wed Nov 29 18:35:37 PST 2023
Lock tetromino S
Wed Nov 29 18:35:37 PST 2023
Clear 0 lines.
Wed Nov 29 18:35:37 PST 2023
Spawn tetromino O.
Wed Nov 29 18:35:43 PST 2023
Lock tetromino O
Wed Nov 29 18:35:43 PST 2023
Clear 0 lines.
Wed Nov 29 18:35:43 PST 2023
Spawn tetromino J.
Wed Nov 29 18:35:45 PST 2023
Lock tetromino J
Wed Nov 29 18:35:45 PST 2023
Clear 1 lines.
```
