<?php defined('POPLR') or die() ?>
<div class="logo">
	<h1><span><?php echo $poplr->etxt('app.header') ?></span></h1>
</div>
<ul class="menu">
	<?php $poplr->template('layout.languages') ?>
</ul>
<div class="wrapper">
	<div class="block">
		<h2 class="icon-how-to-play"><?php echo $poplr->etxt('app.dislike.title') ?></h2>
		<?php if ($poplrAction->thankYou) : ?>
			<blockquote>
				<h3><?php echo $poplr->etxt('app.dislike.thankYou') ?>
			</blockquote>
		<?php else : ?>
			<form method="post">
				<textarea
					name="response"
					rows="5"
					cols="30"
					style="display:block;width:100%;"
					placeholder="<?php echo $poplr->etxt('app.dislike.placeholder') ?>"
				></textarea>
				<input
					style="display:block;width:100%;margin-top:8px;"
					name="send"
					type="submit"
					value="<?php echo $this->etxt('app.dislike.send') ?>"
				/>
			</form>
		<?php endif ?>
	</div>
</div>
<div class="footer">
	&copy; ZameDev, 2013
</div>
