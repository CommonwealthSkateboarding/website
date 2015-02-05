$(document).ready(function(){

	var caro = $("#carousel");
	var header = $("header#main");
	
	caro.slick({
		autoplay: true,
		autoplaySpeed: 3000,
		arrows: false,
		slidesToScroll: 1,
		dots: true,
		infinite: true,
		speed: 1500,
		slide: '.slide',
		cssEase: 'ease-out',
		pauseOnHover: true,
		fade: true,

	// 	responsive: [
	// 	{
	// 		breakpoint: 480,
	// 		settings: {
	// 			autoplay: false
	// 		}
	// 	}
	// ]
	});

	// Custom Carousel Arrows
	$('.prev.arrow').click(function() {
		caro.slickPrev();
	});

	$('.next.arrow').click(function() {
		caro.slickNext();
	});

	// Toggle Removal of Carousel @ 600px
	var mq6 = matchMedia("screen and (max-width: 600px)");
	mq6.addListener(function(mq6) {
			if (mq6.matches) {
				caro.detach();
			} else {
				caro.insertAfter(header);
			}
	});

	// Remove Carousel Altogether on Mobile
	var mqm = matchMedia("screen and (max-device-width: 420px)"),
		handleMQL = function(mqm) {
		if (mqm.matches) {
			caro.detach();
		}
	};
	handleMQL(mqm);
	mqm.addListener(handleMQL);
		

});