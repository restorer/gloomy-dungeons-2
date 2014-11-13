<?php defined('POPLR') or die() ?>
<ul class="menu">
	<?php $poplr->template('layout.languages') ?>
</ul>
<?php if ($poplrAction->isValid) : ?>
	<div class="logo">
		<h1><a href="<?php echo $poplr->actionUrl('index') ?>"><span><?php echo $poplr->etxt('app.header') ?></span></a></h1>
		<p><?php echo $poplr->txt('app.index.teaser') ?></p>
	</div>
	<div class="wrapper">
		<div class="block" style="overflow:hidden;">
			<div style="float:right">
				<h2 style="padding:0;text-align:center;">
					<a href="<?php echo $poplr->actionUrl('index') ?>"><?php echo $poplr->etxt('app.index.menu.home') ?></a>
				</h2>
				<h2 style="padding:0;text-align:center;">
					<a href="https://play.google.com/store/apps/details?id=org.zamedev.gloomydungeons2.gplay&amp;utm_medium=referral&amp;utm_source=gloomy2site&amp;utm_campaign=apps">
						<?php echo $poplr->etxt('app.index.menu.googlePlay') ?>
					</a>
				</h2>
				<a href="http://goo.gl/xDBmL"><img src="<?php echo $poplr->getThemedUrl('images/qr-big.png') ?>" /></a>
			</div>
			<h2 class="icon-play-in-browser"><?php echo $poplr->etxt('app.userinfo.player') ?></h2>
			<blockquote>
				<h3><?php echo htmlspecialchars($poplrAction->name) ?></h3>
			</blockquote>
			<h2 class="icon-options"><?php echo $poplr->etxt('app.userinfo.exp') ?></h2>
			<blockquote>
				<h3><?php echo $poplrAction->exp ?></h3>
			</blockquote>
			<?php if (count($poplrAction->achievements)) : ?>
				<h2 class="icon-controls"><?php echo $poplr->etxt('app.userinfo.achieved') ?></h2>
				<blockquote><table>
					<?php foreach ($poplrAction->achievements as $item) : ?>
						<tr>
							<th class="highlight"><?php echo htmlspecialchars($item['title']) ?></th>
							<td><?php echo htmlspecialchars($item['description']) ?></td>
						</tr>
					<?php endforeach ?>
				</table></blockquote>
			<?php endif ?>
		</div>
	</div>
<?php else : ?>
	<div class="logo">
		<h1><span><?php echo $poplr->etxt('app.header') ?></span></h1>
		<p>Invalid user id</p>
	</div>
<?php endif ?>
<div class="footer">
	&copy; ZameDev, 2013
</div>
