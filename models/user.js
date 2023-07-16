import mongoose from 'mongoose'
import { MongoClient } from 'mongodb';
import users from '../app.js';

const userSchema = new mongoose.Schema({
    username: {
        type: String,
        required: true,
        unique: true
    },
    password: {
        type: String,
        required: true
    },
    displayName: {
        type: String,
        required: true,
        unique: false
    },
    profilePic: {
        type: String,
        required: true,
        unique: false
    },
    chats: [{
        id: {
            type: Number,
            required: false,
        },
        user: {
            username: {
                type: String,
                required: true,
                unique: true
            },
            displayName: {
                type: String,
                required: true,
                unique: false
            },
            profilePic: {
                type: String,
                required: true,
                unique: false
            },
        },
        lastMessage: {
            id: {
                type: Number
            },
            created: {
                type: Date
            },
            content: {
                type: String
            }
        }
    }],
    messages: [{
        id: {
            type: Number
        },
        created: {
            type: Date
        },
        sender: {
            username: {
                type: String
            }
        },
        content: {
            type: String
        }
    }]
});

async function check(username, password) {
    const result = await users.find({ username: username, password: password }).toArray();
    if (result.length !== 0) {
        return true;
    }
    return false;
}
export default check;
const User = mongoose.model('User', userSchema);

async function addUser(username, password, displayName, profilePic) {
    try {
        
        // const client = new MongoClient("mongodb://localhost:27017");

        // const db = client.db("ChatWeb");
        // const users=db.collection("Users");
        const lastUser = await users.findOne({}, { sort: { id: -1 } });
        let id = 1;
        if (lastUser) {
            id = lastUser.id + 1;
        }

        const newUser = { id, username, password, displayName, profilePic, chats: [], messages: [] };
        
        // users=db.collection("Users");
        const result = await users.insertOne(newUser);


    } catch (error) {
        console.log('An error occurred:', error);
    }
}
async function getUser(username) {
    const result = await users.findOne({ username });
    if (result.length == 0) {
        return null;
    }
    return { username: result.username, displayName: result.displayName, profilePic: result.profilePic };
}
async function getChats(username) {
    const result = await users.findOne({ username });

    return result.chats;
}
async function getMessages(username, chat) {
    var mArr = [];
    const result = await users.findOne({ username });
    if (!result) {
        return null;
    }
    for (const messageKey in result.messages) {
        const message = result.messages[messageKey];
        if (message.id.toString() === chat.toString()) {
            mArr.push(message);
        }
    }
    return mArr
}
async function addMessage(username, chatid, content) {
    const currentDate = new Date();

    // Get the current date
    const year = currentDate.getFullYear(); // 4-digit year
    const month = currentDate.getMonth() + 1; // Month (0-11, so add 1 to get 1-12)
    const day = currentDate.getDate(); // Day of the month (1-31)

    // Get the current time
    const hours = currentDate.getHours(); // Hours (0-23)
    const minutes = currentDate.getMinutes(); // Minutes (0-59)
    const seconds = currentDate.getSeconds(); // Seconds (0-59)

    // Create the formatted date and time string
    const dateTimeString = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;

    const message = { id: chatid, created: dateTimeString, sender: { username }, content };
    const result = await users.findOne({ username });
    if (!result) {
        return null;
    }
    const messages = result.messages;
    messages.push(message);
    await users.updateOne({ username }, { $push: { messages: message } });

    const otherUser = result.chats[chatid - 1].user.username

    console.log(username + "sent to " + otherUser)

    const result2 = await users.findOne({username: otherUser });
    if (!result2) {
        //console.log("fu")
        return null;
    }
    const msgs2 = result2.messages;
    const chats2 = result2.chats;
    for(let i = 0; i < chats2.length; i++) {
        if(chats2[i].user.username === username) {
            const message2 = { id: i + 1, created: dateTimeString, sender: { username }, content };
            msgs2.push(message2);
            await users.updateOne({ username: otherUser }, { $push: { messages: message2 } });
            return;
        }
    }

}
async function addChat(username, user) {

    var result = await users.findOne({ username });
    if (!result) {
        return null;
    }
    var contact = await users.findOne({ username: user });
    if (!result) {
        return null;
    }

    var chasts = result.chats;
    chasts.sort((a, b) => b.id - a.id);

    var lastChat = chasts[0];
 
    let id = 1;
    if (lastChat) {
        id = lastChat.id + 1;
    }
    const chat = { id, user: { username: contact.username, displayName: contact.displayName, profilePic: contact.profilePic }, lastMessage: null }
    var chats = result.chats;
    chats.push(chat);
    await users.updateOne({ username }, { $push: { chats: chat } });
    //add the caht to the 
    chasts = contact.chats;
    chasts.sort((a, b) => b.id - a.id);
    lastChat = chasts[0];
 
     id = 1;
    if (lastChat) {
        id = lastChat.id + 1;
    }
    const chat2 = { id, user: { username: result.username, displayName: result.displayName, profilePic: result.profilePic }, lastMessage: null }
     chats = contact.chats;
     chats.push(chat2);
    await users.updateOne({ username:contact.username }, { $push: { chats: chat2 } });
}

export {
    addUser,
    getChats,
    getMessages,
    check,
    getUser,
    addMessage,
    addChat
}