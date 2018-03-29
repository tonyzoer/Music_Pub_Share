package com.zoer.musicserver.data

data class Song (var id:Int,
                 var artist:String,
                 var title:String,
                 var album:String,
                 var genre:String,
                 var source:String,
                 var image:String,
                 var trackNumber:Int,
                 var totalTrackCount:Int,
                 var duration:Int,
                 var site:String="")