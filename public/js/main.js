$(document).ready(function() {
  var caro = $("#carousel");
  var header = $("header#main");

  var mnav = $("nav#mobile");
  var mnav_toggle = $(".mobile__nav-toggle");
  var mnav_content = $(".mobile__nav-content");
  var mnav_items = $(".mobile__nav-items");

  mnav_toggle.click(function() {
    $(this).toggleClass("active");
    $("[data-icon]").toggleClass(function() {
      if ($(this).is(".fa-bars")) {
        return "fa-times";
      } else {
        return "fa-bars";
      }
    });
    $(mnav_items).toggleClass("show");
    $(mnav_content).toggleClass("visible");
  });

  if ($(mnav).is(":visible")) {
    $(".alert.full-width").addClass("mobile");
    $(".logo--extended")
      .removeClass("hide")
      .addClass("mobile show");
    $("#content").addClass("mobile");
  }

  if ($(".alert").is(":visible")) {
    $(".logo--extended").addClass("alert");
  }

  // Apply .active to clicked nav item
  $(
    'header#main nav a[href$="/' + location.pathname.split("/")[1] + '"]'
  ).addClass("active");

  // Slick.js Slider Settings
  // caro.slick({
  //   autoplay: true,
  //   autoplaySpeed: 3900,
  //   arrows: false,
  //   slidesToScroll: 1,
  //   dots: true,
  //   infinite: true,
  //   speed: 600,
  //   slide: ".slide",
  //   pauseOnHover: true,
  //   fade: true
  // });

  // Custom Carousel Arrows
  $(".prev.arrow").click(function() {
    caro.slickPrev();
  });

  $(".next.arrow").click(function() {
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
  var mqm = matchMedia(
      "screen and (max-device-width: 420px) and (max-width: 600px)"
    ),
    handleMQL = function(mqm) {
      if (mqm.matches) {
        caro.detach();
      }
    };
  handleMQL(mqm);
  mqm.addListener(handleMQL);

  // Scroll to Anchor on Modal Close
  function scrollToAnchor(id) {
    var aTag = $("a[id='" + id + "']");
    $("html,body").animate({ scrollTop: aTag.offset().top - 400 }, "slow");
  }

  // Close Popup Modals
  $("#close-modal").click(function(e) {
    e.preventDefault();
    $(".modal").fadeOut(100);
    $("body").removeClass("modal-open");
    scrollToAnchor("terms-link");
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
  $("table.clickable tr td").on("mousedown", function(e) {
    clickableRowListener(this, e);
  });

  function clickableRowListener(that, e) {
    var c = $(that).html(),
      url =
        $(that)
          .parent()
          .data("href") ||
        $(that)
          .parent()
          .find("a")
          .attr("href");
    if (
      url &&
      c.indexOf("<a") == -1 &&
      c.indexOf("<button") == -1 &&
      c.indexOf("<input") == -1
    ) {
      $(that).html(
        "<a href='" +
          url +
          "' target='" +
          (e.ctrlKey || e.which === 2 ? "_blank" : "") +
          "'>" +
          c +
          "</a>"
      );
      var a = $(that).find("a");
      if (e.which === 3) {
        setTimeout(function() {
          a.parent().html(c);
        }, 300);
      } else {
        a[0].click();
        a.parent().html(c);
      }
    }
  }
});
