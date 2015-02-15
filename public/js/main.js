$(document).ready(function(){
		
	var caro = $("#carousel");
	var header = $("header#main");

	// Apply .active to clicked nav item
	$('header#main nav a[href$="/' + location.pathname.split("/")[1] + '"]').addClass('active');

	// Smooth scroll + center to anchors
	$('a[href^="#"]').on('click', function(event) {
    var target = $(this.href);
    if( target.length ) {
        event.preventDefault();
        $('html, body').animate({
            scrollTop: target.offset().top
        }, 1000);
    }
	});

	// Slick.js Slider Settings
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
	var mqm = matchMedia("screen and (max-device-width: 420px) and (max-width: 600px)"),
		handleMQL = function(mqm) {
		if (mqm.matches) {
			caro.detach();
		}
	};
	handleMQL(mqm);
	mqm.addListener(handleMQL);
		

});