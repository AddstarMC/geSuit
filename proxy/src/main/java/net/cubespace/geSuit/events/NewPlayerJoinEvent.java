/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.cubespace.geSuit.events;

/**
 *
 * @author JR
 */
public class NewPlayerJoinEvent
{

    private final String message;
    private final String player;

    public NewPlayerJoinEvent(String playerName, String message)
    {
        this.player = playerName;
        this.message = message;
    }

    public String getMessage()
    {
        return message;
    }

    public String getPlayer()
    {
        return player;
    }
}
