# Picture voting application

We'll be building a picture voting application with burner.

## Requirements

Burner has webhooks. The idea there is that a developer can plug his/her api into a burner and get events about that burner (inbound text, inbound media, voicemail). Please visit http://developer.burnerapp.com for more info.

We want to build a picture voting application on top of phone numbers. When people send a picture message to the number we'll get the appropriate event with picture url and store the picture in a folder in dropbox. People can also vote for a picture (assuming user will share that folder for everybody to see the pictures) by texting it's name, pic1.jpg for example. If the text is one of the pictures we have, then we add one vote for that picture. We'll also provide a way to report how many votes each picture gets.

To make it simpler we won't handle following cases for our initial version:

* User having more than one number. Right now we'll assume user has one number connected to our endpoint.
* Limit how many times someone can vote for a picture. Technically we can see that fromNumber +14444444 already voted for pic1.jpg and not add a vote for that picture but we're gonna skip it for the time being.

## Implementation

We'll write a rest api to implement this in Scala. Api should have following endpoints:

POST /event
This endpoint will be called from burner and get the json events mentioned above.

GET /report
This endpoint should return a json dictionary where key is picture name in dropbox folder and value is number of votes it has.

### Notes
Implementation will use rest calls to dropbox instead of using some java sdk.
Provide a way to easitly review your code. Having it committed in a public git repo will be sufficient.
Provide instructions how to specify dropbox token, dropbox folder, how to run the api you built, etc (One paragraph in README should be enough).

### Testing
Hopefully once you have it written you can get an MMS burner, connect developer connection and put the url to your service (to have a publicly accessible url of a service running in your dev laptop you can use the free version of ngrok).
_Provide your phone number before you start and we'll be happy to give you some free burner credits you can use to buy a burner._

## Helpful information

Documentation for burner developer connection
> http://developer.burnerapp.com

Where to get ngrok (if you need it)
> https://ngrok.com/

To get dropbox token
> https://blogs.dropbox.com/developers/2014/05/generate-an-access-token-for-your-own-account/

To save a file to dropbox
> https://www.dropbox.com/developers-v1/core/docs#save-url

To get list of the files in dropbox folder
> https://www.dropbox.com/developers-v1/core/docs#metadat
a
