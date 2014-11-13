<?php defined('POPLR') or die() ?>
<a name="home"></a>
<div class="logo">
	<h1><span><?php echo $poplr->etxt('app.header') ?></span></h1>
	<p><?php echo $poplr->txt('app.index.teaser') ?></p>
</div>
<ul class="menu">
	<?php $poplr->template('layout.languages') ?>
	<li><a
		style="color:#FFF;"
		href="https://play.google.com/store/apps/details?id=org.zamedev.gloomydungeons2.gplay&amp;utm_medium=referral&amp;utm_source=gloomy2site&amp;utm_campaign=apps"
	><?php
		echo $poplr->etxt('app.index.menu.googlePlay')
	?></a></li>
	<li><a href="#home"><?php echo $poplr->etxt('app.index.menu.home') ?></a></li>
	<li><a href="#screenshots"><?php echo $poplr->etxt('app.index.menu.screenshots') ?></a></li>
	<li><a href="#how-to-play"><?php echo $poplr->etxt('app.index.menu.howToPlay') ?></a></li>
	<li><a href="#controls"><?php echo $poplr->etxt('app.index.menu.controls') ?></a></li>
	<li><a href="#play"><?php echo $poplr->etxt('app.index.menu.play') ?></a></li>
	<li><a href="#video"><?php echo $poplr->etxt('app.index.menu.video') ?></a></li>
</ul>
<a name="screenshots"></a>
<div class="slider">
	<div class="images">
		<div class="inner-wrap">
			<div class="inner">
				<div class="img"><img src="<?php echo $poplr->getThemedUrl('images/shot-01.jpg') ?>" alt="" /></div>
				<div class="img"><img src="<?php echo $poplr->getThemedUrl('images/shot-02.jpg') ?>" alt="" /></div>
				<div class="img"><img src="<?php echo $poplr->getThemedUrl('images/shot-03.jpg') ?>" alt="" /></div>
				<div class="img"><img src="<?php echo $poplr->getThemedUrl('images/shot-04.jpg') ?>" alt="" /></div>
				<div class="img current"><img src="<?php echo $poplr->getThemedUrl('images/shot-05.jpg') ?>" alt="" /></div>
				<div class="img"><img src="<?php echo $poplr->getThemedUrl('images/shot-06.jpg') ?>" alt="" /></div>
				<div class="img"><img src="<?php echo $poplr->getThemedUrl('images/shot-07.jpg') ?>" alt="" /></div>
				<div class="img"><img src="<?php echo $poplr->getThemedUrl('images/shot-08.jpg') ?>" alt="" /></div>
				<div class="img"><img src="<?php echo $poplr->getThemedUrl('images/shot-09.jpg') ?>" alt="" /></div>
			</div>
		</div>
	</div>
	<div class="bullets">
		<b></b>
		<b></b>
		<b></b>
		<b></b>
		<b class="current"></b>
		<b></b>
		<b></b>
		<b></b>
		<b></b>
	</div>
</div>
<div class="wrapper">
	<div class="block">
		<div class="banners">
			<div class="banner-block">
				<a class="title" href="#video"><?php echo $poplr->txt('app.index.banner.video') ?></a>
				<a class="img-wrap" href="#video"><img class="b-video" src="<?php echo $poplr->getThemedUrl('images/banner-video.jpg') ?>" alt="" /></a>
			</div>
			<div class="banner-separator">
				<?php echo $poplr->txt('app.index.banner.or') ?>
			</div>
			<div class="banner-block">
				<a class="title" href="#play"><?php echo $poplr->txt('app.index.banner.play') ?></a>
				<a class="img-wrap" href="#play"><img src="<?php echo $poplr->getThemedUrl('images/banner-play.jpg') ?>" alt="" /></a>
			</div>
		</div>
	</div>
	<div class="block" style="text-align:center;">
		<a href="http://goo.gl/xDBmL"><img src="<?php echo $poplr->getThemedUrl('images/qr-big.png') ?>" /></a>
	</div>
	<a name="how-to-play"></a>
	<div class="block">
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
	</div>
	<a name="play"></a>
	<div class="block block-nobotpad">
		<h2 class="icon-play-in-browser"><?php echo $poplr->txt('app.index.play.title') ?></h2>
	</div>
</div>
<div class="play-wrapper">
	<div class="play-block">
		<div id="viewport"><a href="javascript:;" id="start-gloomy"><?php echo $poplr->txt('app.index.play.start') ?></a></div>
		<div class="controls-wrapper" style="display:none;">
			<div class="control" id="control-forward"><span>W</span></div>
			<div class="control" id="control-backward"><span>S</span></div>
			<div class="control" id="control-left"><span>A</span></div>
			<div class="control" id="control-right"><span>D</span></div>
			<div class="control" id="control-action"><span>SPACE</span></div>
			<div class="control" id="control-sw-pist"><span>1</span></div>
			<div class="control" id="control-sw-shtg"><span>2</span></div>
			<div class="control" id="control-sw-chgn"><span>3</span></div>
			<div class="control" id="control-sw-hand"><span>0</span></div>
			<div class="control" id="control-map" style="display:none;"><span>M</span></div>
		</div>
	</div>
</div>
<div class="wrapper wrapper-nobg-toppad">
	<a name="video"></a>
	<div class="block block-nobotpad">
		<h2 class="icon-video"><?php echo $poplr->txt('app.index.video.title') ?></h2>
	</div>
</div>
<div class="video-wrapper">
	<div class="video-block">
		<iframe src="http://www.youtube.com/embed/kDVjsX7dVCE" frameborder="0" allowfullscreen="allowfullscreen"></iframe>
	</div>
</div>
<div class="footer">
	&copy; ZameDev, 2013
</div>

<script type="text/javascript">
	window.root = <?php echo json_encode($poplr->httpRoot) ?>;

	window.loc = <?php echo json_encode(array(
		'cant_open' => $poplr->txt('app.index.gloomy.cantOpen'),
		'need_blue_key' => $poplr->txt('app.index.gloomy.needBlueKey'),
		'need_red_key' => $poplr->txt('app.index.gloomy.needRedKey'),
		'need_green_key' => $poplr->txt('app.index.gloomy.needGreenKey'),
		'secret_found' => $poplr->txt('app.index.gloomy.secretFound'),
	)) ?>;
</script>
