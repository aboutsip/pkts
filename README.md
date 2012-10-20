# About SIP

# What "About SIP" contains

Currently, About SIP contains the following two libraries:

* aboutsip-buffers - yet another java buffer implementation inspired by netty.io. This implementation is 100% standalone and is only dependent on java 6
* yajpcap - yet another java pcap library, which provides a library in pure java for reading pcap files and the only dependency is on aboutsip-buffers.

To get more information about each sub-project, please see their respective readme files.  They will contain a brief introduction to the project as well as an architectural overview to help you get coding asap.

# Get Started

Everything within About SIP is following the general guidelines of Maven so if you are familiar with Maven you should have no problem getting started. Currently, none of the projects within About SIP exists in any public maven repository so for now you will have to build it yourself, something that should be fast and easy.

## Build it

1. Clone it
1. cd aboutsip/modules
1. mvn install
1. done

## Import the code into your favorite IDE

Most Java IDE:s are capable of importing Maven based projects, allowing for a smooth integration with the IDE and Maven. Remember, Maven is the source of truth so please do not check in any IDE specific files (such as .project and .classpath for Eclipse).

If you are using Eclipse, see instructions here.

If you are using another IDE, feel free to add instructions in this file of how to import the project into it.

## Start coding!

So, you have succerssfully loaded all the code so what is the next steps. Well, check out the readme files under each sub-project to get a description of what that module is supposed to do and how to get started with the project. If you want to contribute to any project, please read the section about contribution, specifially, please checkout the section about coding principals and the style guide.



Contributions are more than welcome and if you have succeeded in building it, then you are off to a good start. If you have ideas of your own, please file a ticket so when you do submit a pull request, we know what the intent of this feature is, this also gives us a way of communicating about features.

If you just want to get your hands dirty then feel free to check out the open issues here. If there is anything you would like to start tackling, just assign the task to yourself and get cranking. 

## Code Principals

At aboutsip, we try to follow these general principals and by adhering to these simple guidelines, we feel that our code is readable, easy to follow and is testable (even though we can always do better so please give us constructive feedback):
Write code so others can read and understand it.
Write testable code.
Unit tests everything, but no need to strive for 100%. Try to hit the 85% mark.
A method should not be longer than a page (if it is, you probably can split it up).
Javadoc everything! 
Please describe what each class/interface is supposed to do. If you cannot explain it, then no one else will be able to understand it either.
Each method should be documented. Describe what the method is supposed to do. If you end up writing an essay about the method, perhaps it is too complicated? 


## Style Guide

Please following the style already in place. If you are an Eclipse user, follow the guidelines for Eclipse below and please load the formatting template and then you won't even have to think about it, Eclipse will do it for you.

If you are using another IDE then please adhere to these basic rules:

In general, the aboutsip code base is following regular java style convention but here are the more important points (and some differences)
No tabs – ever
Indent is 4 spaces
The line width is 120 characters so please do not cut it off at 80 (we all have screens with high enough resolution these days).
Use final everywhere! (once again, you can have Eclipse fix this for you).
Use this everywhere! (Eclipse will fix it for you)

