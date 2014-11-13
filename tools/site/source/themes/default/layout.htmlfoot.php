<?php defined('POPLR') or die() ?>
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
<script>window.jQuery || document.write('<script src="js/jquery.js"><\/script>')</script>
<?php foreach ($poplr->scripts['foot'] as $url) : ?>
	<script type="text/javascript" src="<?php echo htmlspecialchars($url) ?>"></script>
<?php endforeach ?>
<script>
(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
})(window,document,'script','//www.google-analytics.com/analytics.js','ga');
ga('create', 'UA-1782921-23', 'zame-dev.org');
ga('send', 'pageview');
</script>
