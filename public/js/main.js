$(document).ready(function(){

	$('#carousel').slick({
		autoplay: true,
		autoplaySpeed: 5000,
		arrows: false,
		slidesToScroll: 1,
		dots: true,
		infinite: true,
		speed: 500,
		slide: '.slide',
		cssEase: 'linear',
		pauseOnHover: false,
		fade: true
	});

	$('.prev.arrow').click(function() {
		$('#carousel').slickPrev();
	});

	$('.next.arrow').click(function() {
		$('#carousel').slickNext();
	});

	$("#datepicker").datepicker();

    $("#newContent").cleditor({});

    $("#newExtendedContent").cleditor({});

});