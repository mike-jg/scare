# Scare

This is a small game I built several years ago. It was done as a fun/learning experience, and as such doesn't use any libraries.

# Gameplay

Scare is a top down shooter, where you are placed into total darkness and must use your torch/explosions to see.

The aim is to get to the end of the level, which is marked by pink tiles on the floor. You will then be moved on to the next level.

Use WASD to move, the mouse aims. Left click to fire. Number keys change weapon.

Try to kill the zombies before they get too close.

# Build

There are no build scripts, but this should get it running in IntelliJ IDEA:

+ Create a new run configuration based on 'Application'
+ Set the main class to be 'scare.Scare'

# Notes

+ As I made the sprites in Paint, it's hardcoded to run at a certain size. As such it can be difficult to see at higher resolutions (at the time I only had a low res monitor).
+ The aiming seems to not quite work right if you hold the mouse button down, you may need to mash instead
+ I didn't write the AStar algorithm, though maybe I should go back and do that at some point
+ (Warning) It has sounds which can be quite loud