<div class="logo">
	<h1><span><?php echo $poplr->etxt('app.header') ?></span></h1>
	<p><?php echo $poplr->txt('app.index.teaser') ?></p>
</div>
<ul class="menu">
	<?php $poplr->template('layout.languages') ?>
	<li><a href="#how-to-play"><?php echo $poplr->etxt('app.index.menu.howToPlay') ?></a></li>
	<li><a href="#options"><?php echo $poplr->etxt('app.index.menu.options') ?></a></li>
	<li><a href="#controls"><?php echo $poplr->etxt('app.index.menu.controls') ?></a></li>
</ul>
<a name="how-to-play"></a>
<h2 class="icon-how-to-play"><?php echo $poplr->txt('app.index.howToPlay.title') ?></h2>
<?php echo $poplr->txt('app.index.howToPlay.text') ?>
<div class="images-2col">
	<div class="img-block">
		<span><?php echo $poplr->txt('app.index.howToPlay.mainMenu') ?></span>
		<img src="<?php echo $poplr->getThemedUrl('images/help-menu.png') ?>" alt="" />
	</div>
	<div class="img-block">
		<span><?php echo $poplr->txt('app.index.howToPlay.selectEpisode') ?></span>
		<img src="<?php echo $poplr->getThemedUrl('images/help-episode.png') ?>" alt="" />
	</div>
</div>
<a name="options"></a>
<h2 class="icon-options"><?php echo $poplr->txt('app.index.options.title') ?></h2>
<?php echo $poplr->txt('app.index.options.text') ?>
<a name="controls"></a>
<h2 class="icon-controls"><?php echo $poplr->txt('app.index.controls.title') ?></h2>
<?php echo $poplr->txt('app.index.controls.text') ?>
<div class="images-2col">
	<div class="img-block">
		<span><?php echo $poplr->txt('app.index.controls.controls') ?></span>
		<img src="<?php echo $poplr->getThemedUrl('images/controls-controls.png') ?>" alt="" />
	</div>
	<div class="img-block">
		<span><?php echo $poplr->txt('app.index.controls.zeemote') ?></span>
		<img src="<?php echo $poplr->getThemedUrl('images/controls-zeemote.png') ?>" alt="" />
	</div>
</div>
<div class="footer">
	&copy; ZameDev, 2013
</div>
