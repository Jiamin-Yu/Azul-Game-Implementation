# Description for JSON interfaces

## LOGIN
```
{ 
    "type" : "login",
    "nick" : "<nick>",
    "room name" : "<room name>"
}
```
Annotation:
1. It is sent from a user to the server.
2. It informs the server that a user trys to log in.


```
{
    "type" : "login success"
    "logged users" : 
    [
       {    
           "nick" : "<nick>",
           "is ready for game" : true/false,
       }
    ]
}
```
Annotation:
1. It is sent from the server to the user that just tried to log in.
2. It informs the user that her/his login succeeded.

```
{
    "type" : "login failed"
    "cause" : "<cause>"
}                
```

Annotation:
1. It is sent from the server to the user that just tried to log in.
2. It informs the user that her/his login failed.

## USER JOINED & USER LEFT

```
{
    "type" : "user joined"
    "nick" : "<nick>"
}
```
Annotation:

1. It is sent from the server to all users in a certain room, except the one that just came in.
2. It informs them that a user just joined.

```
{
    "type" : "user left"
    "nick" : "<nick name>"
}

```
Annotation:
1. It is sent from the server to all users in a certain room.
2. It informs them that a user in their room just left.

## READY STATE
```
{
   "type" : "ready for game"
}
```

Annotation:
1. It is sent from a user to the server.
2. It informs the server that the user is ready for game.

```
{
   "type" : "not ready for game"
}

```
Annotation:
1. It is sent from a user to the server.
2. It informs the server that the user is not ready for game.

```
{
    "type" : "ready for game"
    "nick" : "<nick>"
}
```
Annotation:
1. It is sent from the server to all users in a certain room, except the one that is just ready for game.
2. It informs them that a user in their room is ready for game.

```
{
    "type" : "not ready for game"
    "nick" : "<nick>"
}
```
Annotation:
1. It is sent from the server to all users in a certain room, except the one that is just not ready for game.
2. It informs them that a user in their room is not ready for game.

```
{
    "type" : "game start"
    "factory displays" : [[tile]]
    "current player" : <current player>
}
```
Annotation:
1. It is sent from the server to all users in a certain room.
2. It informs them that the game starts and how it is initialized.

## MOVE

```
  {
      "type" : "collect tiles"
      "collect place" : <index of factory display or game table>
      "tile index" : "<tile index>"
  }
```
Annotation:
1. It is sent from a user to the server.
2. It informs the server where s/he collected tiles.
3. The index of the game table is denoted by -1.
4. The tile index is the index of the picked tile for factory display.

```
  {
      "type" :place tiles request"
      "place location" : <index of pattern line or floor line>
  }
```
Annotation:
1. It is sent from a user to the server.
2. It informs the server where s/he wants to place tiles.
3. The index of the floor line is -1

```
{
    "type" : "turn"
    "nick" : "<nick>"
}
```
Annotation:
1. It is sent from the server to all users, except the one that can move.
2. It informs them who can move now.

```
{
    "type" : "valid move"
    "place location" : <index of pattern line or floor line>
    "tiles added on pattern line" : [tile]
    "tiles added on floor line" : [tile]
}
```

Annotation:
1. It is sent from the server to a user.
2. It informs the user that his move is valid.

```
{
    "type" : "invalid move"
}
```
Annotation:
1. It is sent from the server to a user.
2. It informs the user that his move is invalid.

```
{
    "type" : "somebody collected tiles"
    "collect place" : <index of factory display or game table>
    "tile index" : "<tile index>"
}
```
Annotation:
1. It is sent from the server to all users.
2. It informs them that where s/he just collected tiles.
```
{
    "type" : "somebody placed tiles"
    "nick" : "<nick>"
    "place location" : <index of pattern line or floor line>
    "tiles added on pattern line" : [tile]
    "tiles added on floor line" : [tile]
}
```
Annotation:
1. It is sent from the server to all users, except the one that just placed tiles.
2. It informs them which pattern line and the floor line of her or him how changed. 
3. if the pattern line did not change, the index of pattern line will be denoted by -1.

## UPDATE WALL AND SCORE

```
{
    "type" : "update walls and scores"
    "updated content" : [ 
                          { 
                            "updated wall: [ { "row of wall" : <>, "column of wall" : <> } ]
                            "updated score" : <score> 
                          } 
                        ]
}       
```
Annotation:
1. It is sent from the server to all users in the same room.
2. It informs them how walls and scores of all users are updated.

## Start Next Round
```
{
    "type" : "start next round"
    "factory displays" : [[tile]]
    "current player" : <current player>
}
```
Annotation:
1. It is sent from the server to all users in a certain room.
2. It informs them that the next round starts.

## FILL FACTORY DISPLAYS

```
{
   "type" : "fill factory displays"
   "factory displays" : [[tile]]
}
```
Annotation:
1. It is sent from the server to all users.
2. It informs them how to fill or refill the factory displays.

## End of Game
```
{
    "type" : "end of game"
    "rankings" : [ { "player id" : <player id>, "ranking": <ranking> } ]
}
```
Annotation:
1. It is sent from the server to all users.
2. It informs them that the game ended with rankings.

## Quit Game
```
{
    "type" : "quit game"
    "nick" : "<nick>"
}
```
Annotation:
1. It is sent from sever to all users, except the one that just quited game.
2. It informs them that s/he quited the game and the game ends.

## Restart Game
```
{
    "type" : "restart request"
}
```
Annotation:
1. It is sent from a user to the server.
2. It informs the server that s/he wants to restart game.

```
{
    "type" : "restart request"
    "nick" : "<nick>"
}
```
Annotation:
1. It is sent from sever to all users, except the one that wants to restart game.
2. It informs them that s/he wants to restart game.

```
{
    "type" : "reply to restart request"
    "reply" : <reply>
}
```
Annotation:
1. It is sent from a user to the server.
2. It informs the server whether s/he wants to restart game.