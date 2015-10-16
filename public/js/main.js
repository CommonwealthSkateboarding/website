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

	// Scroll to Anchor on Modal Close
	function scrollToAnchor(id){
		var aTag = $("a[id='"+ id +"']");
		$('html,body').animate({scrollTop: aTag.offset().top - 400},'slow');
	}

	// Close Popup Modals
	$('#close-modal').click(function(e) {
		e.preventDefault();
		$('.modal').fadeOut(100);
		$('body').removeClass("modal-open");
		scrollToAnchor('terms-link');
	});

	// ToS Agreement Triggers
	var tos_checkbox = $('#agreement');
	var tos_label = $('label.agreement');
	var pay_btn = $('#pay');

	$(tos_checkbox).change(function() {
		if(this.checked) {
			$(tos_label).addClass('checked');
			$(pay_btn).prop('disabled', false);
		} else {
			$(tos_label).removeClass('checked');
			$(pay_btn).prop('disabled', true);
		}
	});
	
	


	// Instagram 
	// $('#instagram').instagram({
	// 	userId: 'commonwealthskate',
	// 	location: {
	// 	id: 1578775
	// 	},
	// 	clientId: '35443b3e3ebd4d6a954ca1f10f4deba0',
	// 	accessToken: ''
	// });

		
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

	// Google Map
	var myLatLng = new google.maps.LatLng(45.512455, -122.645703);
	var mapOptions = {
		zoom: 13,
		center: myLatLng,
						mapTypeId: google.maps.MapTypeId.ROADMAP,
		styles: [{"featureType":"all","elementType":"labels.text.fill","stylers":[{"saturation":36},{"color":"#000000"},{"lightness":58}]},{"featureType":"all","elementType":"labels.text.stroke","stylers":[{"visibility":"on"},{"color":"#000000"},{"lightness":16}]},{"featureType":"all","elementType":"labels.icon","stylers":[{"visibility":"off"}]},{"featureType":"administrative","elementType":"geometry.fill","stylers":[{"color":"#000000"},{"lightness":20}]},{"featureType":"administrative","elementType":"geometry.stroke","stylers":[{"color":"#000000"},{"lightness":17},{"weight":1.2}]},{"featureType":"landscape","elementType":"geometry","stylers":[{"color":"#000000"},{"lightness":23}]},{"featureType":"poi","elementType":"geometry","stylers":[{"color":"#000000"},{"lightness":21}]},{"featureType":"road.highway","elementType":"geometry.fill","stylers":[{"color":"#000000"},{"lightness":15}]},{"featureType":"road.highway","elementType":"geometry.stroke","stylers":[{"color":"#000000"},{"lightness":15},{"weight":0.2}]},{"featureType":"road.arterial","elementType":"geometry","stylers":[{"color":"#000000"},{"lightness":15}]},{"featureType":"road.local","elementType":"geometry","stylers":[{"color":"#000000"},{"lightness":15}]},{"featureType":"transit","elementType":"geometry","stylers":[{"color":"#000000"},{"lightness":15}]},{"featureType":"water","elementType":"geometry","stylers":[{"color":"#000000"},{"lightness":17}]}]
	};
	var map = new google.maps.Map(document.getElementById("map"), mapOptions);
	var icon = {
			url: "/assets/img/gmap.svg",
			size: new google.maps.Size(60, 60),
			origin: new google.maps.Point(0,0),
			anchor: new google.maps.Point(30,60)
	};
	var marker = new google.maps.Marker({
		position: myLatLng,
		map: map,
		draggable: false,
		icon: icon,
		title: "Commonwealth Skateboarding"
	});
	
	// Map Centered on Resize
	google.maps.event.addDomListener(window, "resize", function() {
			var center = map.getCenter();
			google.maps.event.trigger(map, "resize");
			map.setCenter(center);
	});
});