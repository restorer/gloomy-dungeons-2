<?php

defined('POPLR') or die();

return array(
	'core.fatalError' => 'Fatal error occurred',
	'core.unknownAction' => 'Unknown action',
	'core.loginRequired' => 'This action is available only for registered users',
	'core.noPermissions' => 'Current user has no permissions for current action',
	'core.uploadingDisabled' => 'Uploading images is temporarily disabled',
	'core.commentsDisabled' => 'Commenting is temporarily disabled',
	'db.connectionFailed' => 'Connection failed: %s',
	'db.sqlError' => 'SQL Error: %s',

	'app.header' => 'Gloomy Dungeons 2: Blood Honor',
	'app.index.teaser' => '
Gloomy Dungeons II is a continuation of old&ndash;school 3d&ndash;shooter in the style of Doom and Wolfenstein.<br />
The game, where eight bit graphics is in harmony with the 3D&ndash;effects.
	',
	'app.index.menu.googlePlay' => 'Google Play',
	'app.index.menu.home' => 'Home',
	'app.index.menu.screenshots' => 'Screenshots',
	'app.index.menu.howToPlay' => 'How to play',
	'app.index.menu.options' => 'Options',
	'app.index.menu.controls' => 'Controls',
	'app.index.menu.play' => 'Play in browser',
	'app.index.menu.video' => 'Video',
	'app.index.banner.video' => 'Watch video',
	'app.index.banner.or' => 'OR',
	'app.index.banner.play' => 'Play right now in your browser',
	'app.index.howToPlay.title' => 'How to play',
	'app.index.howToPlay.text' => '
<blockquote>
	<h3>Main menu</h3>
	<blockquote><table><tr>
		<th>Play</th>
		<td>
			After you click <strong>Play</strong> button, you will see the minimap, which will show your progress.
			Click <strong>Continue</strong> button or the minimap to continue the game.
		</td>
	</tr><tr>
		<th>Load</th>
		<td>Press to load previously saved game.</td>
	</tr><tr>
		<th>Save</th>
		<td>
			When you exit the game, your gameplay will be <strong>automatically saved</strong>,
			and the next time you can continue exactly from the same place.
			If you want to save your game apart from this (before a difficult place),
			you can do it in the main menu with the help of <strong>Save</strong> button.
		</td>
	</tr><tr>
		<th>Options</th>
		<td>Press to go to the options.</td>
	</tr><tr>
		<th>Achievements</th>
		<td>Press to go to the achievements and leaderboard.</td>
	</tr><tr>
		<th>Store</th>
		<td>Press to go to the store.</td>
	</tr></table></blockquote>
	<h3>Store</h3>
	<blockquote><table><tr>
		<th>Levels</th>
		<td>
			Here you can buy additional episodes. Not all of the episodes are currently available,
			but we are working hard to make them for you.
		</td>
	</tr><tr>
		<th>Upgrade</th>
		<td>If the game seems to be too difficult, or it lacks the drive, then you can upgrade your character here.</td>
	</tr><tr>
		<th>Additional</th>
		<td>Disable ADs? Get to know game codes or change the difficulty of the game? Go there!</td>
	</tr><tr>
		<th>Earn credits</th>
		<td>
			Here are various options to purchase in&ndash;game credits.
			You can either buy them for real money at Google Play store, or get them FREE through our partners.
		</td>
	</tr></table></blockquote>
	<h3>Achievements</h3>
	<blockquote><table><tr>
		<th>Achievements</th>
		<td>Your achievements obtained during the game.</td>
	</tr><tr>
		<th>Leaderboard</th>
		<td>Compare yourself to other players.</td>
	</tr></table></blockquote>
</blockquote>
	',
	'app.index.howToPlay.mainMenu' => 'Main menu',
	'app.index.howToPlay.selectEpisode' => 'Game progress',
	'app.index.options.title' => 'Options',
	'app.index.options.text' => '
<blockquote>
	<h3>General</h3>
	<blockquote><table><tr>
		<th>Help</th>
		<td>Press to open help for the game.</td>
	</tr><tr>
		<th>About and licenses</th>
		<td>Authors and licenses for the free software used in the game.</td>
	</tr><tr>
		<th>Restart game from the very beginning</th>
		<td>If, for any reason, you will want to start over.</td>
	</tr></table></blockquote>
	<h3>Sound</h3>
	<blockquote><table><tr>
		<th>Enable sound</th>
		<td>If you play at night, turn off the sound in order not to frighten the neighbors :)</td>
	</tr><tr>
		<th>Music volume</th>
		<td>The volume of background music.</td>
	</tr><tr>
		<th>Effects volume</th>
		<td>The volume of shots and other sounds.</td>
	</tr></table></blockquote>
	<h3>Controls</h3>
	<blockquote><table><tr>
		<th>Control scheme</th>
		<td>Here you can select control scheme if you do not like the current one.</td>
	</tr><tr>
		<th>Controls setting</th>
		<td>Although we originally picked the most optimal settings, you may want to customize them for yourself.</td>
	</tr><tr>
		<th>Zeemote</th>
		<td>Settings for Zeemote joysticks.</td>
	</tr><tr>
		<th>Key mappings</th>
		<td>If there are hardware buttons in your phone or tablet, then you can map them here.</td>
	</tr></table></blockquote>
	<h3>Screen</h3>
	<blockquote><table><tr>
		<th>Smoothing level</th>
		<td>
			Texture smoothing level (if you have old phone, select first level, for modern phones smoothing has almost no affect on FPS).
			To our taste, it is better to leave the second smoothing level, in order to preserve retro atmosphere better.
		</td>
	</tr><tr>
		<th>Gamma</th>
		<td>If even at maximum brightness of screen the image is too dark, here you can fix it.</td>
	</tr><tr>
		<th>Show crosshair</th>
		<td>Although, following the canons of Doom you will not have to aim precisely, you can still turn on the sight.</td>
	</tr><tr>
		<th>Map position</th>
		<td>Position of the minimap to the center of the screen.</td>
	</tr><tr>
		<th>Rotate in&ndash;game screen</th>
		<td>
			Rotate the screen by 180 degrees. It is useful if there are hardware buttons (D&ndash;Pad or trackball)
			in your phone and you want to play with you left hand.
		</td>
	</tr></table></blockquote>
	<h3>Controls setting</h3>
	<blockquote><table><tr>
		<th>Move speed</th>
		<td>Move speed set-up (forward/backward).</td>
	</tr><tr>
		<th>Strafe speed</th>
		<td>Strafe speed set-up (left/right).</td>
	</tr><tr>
		<th>Rotate speed</th>
		<td>Rotate speed set-up.</td>
	</tr><tr>
		<th>Invert vertical look</th>
		<td>Select to invert vertical look.</td>
	</tr><tr>
		<th>Invert horizontal look</th>
		<td>Select to invert horizontal look (rotating).</td>
	</tr><tr>
		<th>Left hand aim</th>
		<td>Select to place the fire button on the left (and on&ndash;screen joystick on the right).</td>
	</tr><tr>
		<th>Fire button at top</th>
		<td>Select to place the fire button on the top (useful if there is a problem with multitouch).</td>
	</tr><tr>
		<th>Controls scale</th>
		<td>Adjust the size of on&ndash;screen controls.</td>
	</tr><tr>
		<th>Controls opacity</th>
		<td>Transparency of on&ndash;screen controls, from almost invisible to fully opaque.</td>
	</tr><tr>
		<th>Enable accelerometer</th>
		<td>Select for rotation with the accelerometer.</td>
	</tr><tr>
		<th>Accelerometer acceleration</th>
		<td>How quickly a player will rotate using the accelerometer.</td>
	</tr><tr>
		<th>Trackball acceleration</th>
		<td>How quickly a player will move by controlling with the help of trackball (if it is in your phone).</td>
	</tr></table></blockquote>
</blockquote>
	',
	'app.index.controls.title' => 'Controls',
	'app.index.controls.text' => '
<p>
	Since the release of the first version of the game, we have revised our views on the controls,
	and in this release we made them similar to controls of the games Shadowgun and Dead Trigger, which had become the de facto.
</p>
<p>
	In the lower left corner is on&ndash;screen joystick, with which you can move forward, backward, left and right.
	In the lower right corner is fire button.
</p>
<p>
	The entire right side of the screen can be used for rotating &mdash; you should put a finger on the screen and move it to the left / right.
	If you initially put your finger on the fire button, you can rotate and shoot at the same time.
</p>
<p>
	In the upper left corner is game menu button. Here, you can change your weapon, enter the game code or exit to the main menu.
	Next to it are the indicators of health, armor and ammo. Beneath them taken keys indicators will be shown.
</p>
<p>
	In the upper right corner there is button, with the help of which you can toggle minimap.
	The places you have visited and direction indicators are displayed on it.
</p>
	',
	'app.index.controls.controls' => 'Controls',
	'app.index.controls.zeemote' => 'Zeemote',
	'app.index.play.title' => 'Play right now in your browser',
	'app.index.play.start' => 'Start',
	'app.index.video.title' => 'Watch video',
	'app.index.gloomy.cantOpen' => 'Can’t open this door',
	'app.index.gloomy.needBlueKey' => 'You need a blue key',
	'app.index.gloomy.needRedKey' => 'You need a red key',
	'app.index.gloomy.needGreenKey' => 'You need a green key',
	'app.index.gloomy.secretFound' => 'Secret found',

	'app.userinfo.player' => 'Player',
	'app.userinfo.exp' => 'Expirience',
	'app.userinfo.achieved' => 'Achieved',

	'app.dislike.title' => 'Dislike? Tell us why',
	'app.dislike.placeholder' => 'Tell us what we can improve in the game',
	'app.dislike.send' => 'Click here to send response',
	'app.dislike.thankYou' => 'Thank you for your response',
);
