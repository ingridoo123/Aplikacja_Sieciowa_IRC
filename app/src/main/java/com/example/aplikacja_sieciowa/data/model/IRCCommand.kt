package com.example.aplikacja_sieciowa.data.model

sealed class IRCCommand(val commandText: String) {
    data class Nick(val nickname: String) : IRCCommand("NICK $nickname")
    data class Join(val channel: String) : IRCCommand("JOIN $channel")
    data class Leave(val channel: String) : IRCCommand("LEAVE $channel")
    data class Message(val channel: String, val text: String): IRCCommand("MSG $channel $text")
    data class PrivateMessage(val nick: String, val text: String): IRCCommand("PRIVMSG $nick $text")
    data object List: IRCCommand("LIST")
    data class Users(val channel: String) : IRCCommand("USERS $channel")
    data object Help: IRCCommand("HELP")
    data object Quit: IRCCommand("Quit")

}