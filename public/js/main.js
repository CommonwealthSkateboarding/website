$(document).ready(function(){
		
	var caro = $("#carousel");
	var header = $("header#main");
	var mobileNav = $("header#main nav.mobile");

	// Mobile Nav
	mobileNav.click(function() {
		$(this).find("i").toggleClass("fa-list fa-times");
		$(this).toggleClass("expanded");
	});

	if($(mobileNav).is(':visible')) {
		$(".alert.full-width").addClass("mobile");
		$(".logo--extended").addClass("alert");
	}

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
		autoplaySpeed: 3900,
		arrows: false,
		slidesToScroll: 1,
		dots: true,
		infinite: true,
		speed: 600,
		slide: '.slide',
		//cssEase: 'ease-out',
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


	// Instagram 
	// $('#instagram').instagram({
 //    userId: 'commonwealthskate',
 //    location: {
 //   	id: 1578775
 //    },
 //    clientId: '35443b3e3ebd4d6a954ca1f10f4deba0',
 //    accessToken: ''
 //  });

		
	// Clickable Table Rows
	// http://shahinalborz.se/2014/04/solution-make-an-entire-table-row-clickable/
	$('table.clickable tr td').on('mousedown', function (e) {
		clickableRowListener(this, e);
	});

	function clickableRowListener(that, e) {
		var c = $(that).html(), url = $(that).parent().data("href") || $(that).parent().find("a").attr("href");
		if (url && c.indexOf("<a") == -1 && c.indexOf("<button") == -1 && c.indexOf("<input") == -1) {
			$(that).html("<a href='" + url + "' target='" + (e.ctrlKey || e.which === 2 ? "_blank" : "") + "'>" + c + "</a>");
			var a = $(that).find("a");
			if (e.which === 3) {
					setTimeout(function () {
							a.parent().html(c);
					}, 300);
			}
			else {
					a[0].click();
					a.parent().html(c);
			}
		}
	}
});