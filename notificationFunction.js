'use strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

exports.sendNotifications = functions.database.ref('/notifications/{user_id}/{notification_id}').onCreate(event=>{

	const snapshot = event.data;
	const receiver_id = event.params.user_id;
	const sender_id = snapshot.val().from;
	const notificationType = snapshot.val().type;

	 const senderName = admin.database().ref(`users/${sender_id}/name`).once('value');
	 const receiverToken = admin.database().ref(`/users/${receiver_id}/device_token`).once('value');

		return Promise.all([senderName,receiverToken]).then(result => {
				//// promise executes query(pending,fulfilled or rejected) and makes sure it is executed fully////

			const name = result[0].val();	//userQuery array i= 0 as passed]
			const device_token = result[1].val(); //deviceToken array i =1 as passed

			var notify_body, notify_title, clickTarget , id;

			return admin.database().ref(`/friends/${receiver_id}/${sender_id}/chatting`).once('value').then(result =>{

				const chatStatus = result.val();

				if (notificationType == "chat") {

					if (chatStatus == 'true') {

						console.log('users are chatting');
						return;
					}
					notify_body = snapshot.val().message;
					notify_title = name;
					clickTarget = "com.midnight.chatapp.MESSAGE_NOTIFICATION";
					//id = "1";
				}
				else {
					notify_body = `${name} has send you a friend request`;
					notify_title = "New Firend Request";
					clickTarget = "com.midnight.chatapp.REQUEST_NOTIFICATION";
					//id = "2";
				}

				const payload = {
								notification: {
										title: `${notify_title}`,
										body: `${notify_body}`,
										icon: 'default',
										sound: 'default',
										click_action : `${clickTarget}`
									},
									   data : {
										       user_id: `${sender_id}`, ////DATA FOR click_action INTENT////////
													 //notify_id: `${id}`,
													 uName: `${name}`
											 }
								};

			 return admin.messaging().sendToDevice(device_token,payload).then(response => {

				 	console.log('Notification was sent successfully');
			 });
			});

	});

});

///////////////////////PRESENCE FUNCTIION////////////////////////////////

exports.presenceFunction = functions.database.ref('/users/{user_id}/{online}').onUpdate(event => {

	return admin.database().ref('.info/connected').on('value', function(snapshot) {


		if(snapshot.val() == false){
			return;
		}

		const uid = event.params.user_id;

		const dbRef = admin.database().ref(`/users/${uid}`);

					return dbRef.child('online').onDisconnect().set(admin.database.ServerValue.TIMESTAMP).then(response =>{

						console.log('user is online');
					});
			});
});
