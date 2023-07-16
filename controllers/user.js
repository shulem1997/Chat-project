import {check,addUser,getUser,getChats,getMessages,addMessage, addChat} from '../models/user.js'
import jwt from "jsonwebtoken";
import bodyParser from 'body-parser';



// POST /api/Token
async function login (req, res)  {

    if (await check(req.body.username, req.body.password)) {
        const key = "Some super secret key shhhhhhhhhhhhhhhhh!!!!!"
        // Correct username and password - Yayyyy
        // We now want to generate the JWT.
        // The token can contain whatever information we desire.
        // However, do not put sensitive information there, like passwords.
        // Here, we will only put the *validated* username
        const data = { username: req.body.username }
        // Generate the token.
        const token = jwt.sign(data, key)
        // Return the token to the browser
        res.status(201).json({ token });
    }
    else {

        // Incorrect username/password. The user should try again.
        res.status(401).json({
            type: 'https://tools.ietf.org/html/rfc7231#section-6.5.4',
            title: 'Invalid username or password.',
            status: 401,
            errors: {
                message: 'The username and/or password is incorrect.'
            }
        });
    }
};

async function register  (req, res)  {

    await addUser(req.body.username,req.body.password,req.body.displayName,req.body.profilePic)
    const key = "Some super secret key shhhhhhhhhhhhhhhhh!!!!!"
        // The token can contain whatever information we desire.
        // However, do not put sensitive information there, like passwords.
        // Here, we will only put the *validated* username
        const data = { username: req.body.username }
        // Generate the token.
        const token = jwt.sign(data, key)
        // Return the token to the browser
        res.status(201).json({ token });
};

async function getUserInfo (req, res)  {

    
      try {
        const key = "Some super secret key shhhhhhhhhhhhhhhhh!!!!!"
        const auth=req.headers.authorization
        var tokenValue = JSON.parse( auth.match(/Bearer (.*)/)[1]);
        const token = jwt.verify(tokenValue, key)
        // Return the token to the browser
        if(check(token.username)){
            res.status(201).json(await getUser(token.username));
        }
    else {

        // Incorrect username/password. The user should try again.
        res.status(401).json({
            type: 'https://tools.ietf.org/html/rfc7231#section-6.5.4',
            title: 'Invalid username or password.',
            status: 401,
            errors: {
                message: 'The username and/or password is incorrect.'
            }
        });
    }
      } catch (error) {
        // Incorrect username/password. The user should try again.
        res.status(401).json({
            type: 'https://tools.ietf.org/html/rfc7231#section-6.5.4',
            title: 'Invalid username or password.',
            status: 401,
            errors: {
                message: 'The username and/or password is incorrect.'
            }
        });
      }
};
// GET /api/Chats
async function getUserChats (req, res)  {

     try {
        const key = "Some super secret key shhhhhhhhhhhhhhhhh!!!!!"
    const auth=req.headers.authorization
    var tokenValue = JSON.parse( auth.match(/Bearer (.*)/)[1]);
    const token = jwt.verify(tokenValue, key)
    // Return the token to the browser
    if(check(token.username)){
        const chats=await getChats(token.username)
        res.status(201).json( chats);
    }
else {//wrong token

    // Incorrect username/password. The user should try again.
    res.status(401).json({
        type: 'https://tools.ietf.org/html/rfc7231#section-6.5.4',
        title: 'Invalid username or password.',
        status: 401,
        errors: {
            message: 'The username and/or password is incorrect.'
        }
    });
}
     } catch (error) {
        res.status(401).json({
            type: 'https://tools.ietf.org/html/rfc7231#section-6.5.4',
            title: 'Invalid username or password.',
            status: 401,
            errors: {
                message: 'The username and/or password is incorrect.'
            }
     });
    
};
}

async function getUserMessages (req, res)  {

    try {
    const key = "Some super secret key shhhhhhhhhhhhhhhhh!!!!!"
   const auth=req.headers.authorization
   var tokenValue = JSON.parse( auth.match(/Bearer (.*)/)[1]);
   const token = jwt.verify(tokenValue, key)
   // Return the token to the browser
   if(check(token.username)){
       const messages=await getMessages(token.username,req.params.id)
       res.status(201).json( messages);
   }
else {//wrong token

   // Incorrect username/password. The user should try again.
   res.status(401).json({
       type: 'https://tools.ietf.org/html/rfc7231#section-6.5.4',
       title: 'Invalid username or password.',
       status: 401,
       errors: {
           message: 'The username and/or password is incorrect.'
       }
   });
}
    } catch (error) {
       res.status(401).json({
           type: 'https://tools.ietf.org/html/rfc7231#section-6.5.4',
           title: 'Invalid username or password.',
           status: 401,
           errors: {
               message: 'The username and/or password is incorrect.'
           }
    });
   
};
}

async function addUserMessages (req, res)  {

    try {
    const key = "Some super secret key shhhhhhhhhhhhhhhhh!!!!!"
   const auth=req.headers.authorization
   var tokenValue = JSON.parse( auth.match(/Bearer (.*)/)[1]);
   const token = jwt.verify(tokenValue, key)
   // Return the token to the browser
   if(check(token.username)){
       const messages=await addMessage(token.username,req.params.id, req.body.msg)
       res.status(201).json( messages);
   }
else {//wrong token

   // Incorrect username/password. The user should try again.
   res.status(401).json({
       type: 'https://tools.ietf.org/html/rfc7231#section-6.5.4',
       title: 'Invalid username or password.',
       status: 401,
       errors: {
           message: 'The username and/or password is incorrect.'
       }
   });
}
    } catch (error) {
       res.status(401).json({
           type: 'https://tools.ietf.org/html/rfc7231#section-6.5.4',
           title: 'Invalid username or password.',
           status: 401,
           errors: {
               message: 'The username and/or password is incorrect.'
           }
    });
   
};
}

async function addUserChat (req, res)  {

    try {
    const key = "Some super secret key shhhhhhhhhhhhhhhhh!!!!!"
   const auth=req.headers.authorization
   var tokenValue = JSON.parse( auth.match(/Bearer (.*)/)[1]);
   const token = jwt.verify(tokenValue, key)
   // Return the token to the browser
   if(check(token.username)){
       await addChat(token.username,req.body.username,req.body.id, req.body.msg)
       res.status(201).json( );
   }
else {//wrong token

   // Incorrect username/password. The user should try again.
   res.status(401).json({
       type: 'https://tools.ietf.org/html/rfc7231#section-6.5.4',
       title: 'Invalid username or password.',
       status: 401,
       errors: {
           message: 'The username and/or password is incorrect.'
       }
   });
}
    } catch (error) {
       res.status(401).json({
           type: 'https://tools.ietf.org/html/rfc7231#section-6.5.4',
           title: 'Invalid username or password.',
           status: 401,
           errors: {
               message: 'The username and/or password is incorrect.'
           }
    });
   
};
}
export {
    login,
    register,
    getUserInfo,
    getUserChats,
    getUserMessages,
    addUserMessages,
    addUserChat
}