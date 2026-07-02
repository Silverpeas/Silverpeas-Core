document.addEventListener('DOMContentLoaded', function () {


  // Récupérer toutes les images de la galerie
  var imagesSpGallery = [];
  var currentIndex = 0;

  $('.sp-gallery-interaction img').each(function() {
    $(this).attr('title', 'Ouvrir en diaporama / Open slideshow');
    var src = $(this).attr('src');
    var alt = $(this).attr('alt') || 'Sans description';
    imagesSpGallery.push({
      src: src,
      alt: alt
    });
  });

  // Créer le HTML du carrousel
  function createSpCarGal() {
    var img = imagesSpGallery[currentIndex];
    var SpCarGalHTML =
      '<div class="SpCarGal-overlay">' +
        '<div class="SpCarGal-container">' +
          '<button class="SpCarGal-close">&times;</button>' +
          '<button class="SpCarGal-prev">&#10094;</button>' +
          '<div class="SpCarGal-content">' +
            '<img class="SpCarGal-image" src="' + img.src + '" alt="' + img.alt + '">' +
            '<div class="SpCarGal-alt">' + img.alt + '</div>' +
            '<div class="SpCarGal-counter"><span class="current-index">' + (currentIndex + 1) + '</span> / ' + imagesSpGallery.length + '</div>' +
          '</div>' +
          '<button class="SpCarGal-next">&#10095;</button>' +
        '</div>' +
      '</div>';
    return SpCarGalHTML;
  }

  // Ajouter le CSS du carrousel
  var SpCarGalCSS =
    '<style>' +
      '.SpCarGal-overlay {' +
        'position: fixed;' +
        'top: 0;' +
        'left: 0;' +
        'right: 0;' +
        'bottom: 0;' +
        'background-color: rgba(0, 0, 0, 0.9);' +
        'display: flex;' +
        'justify-content: center;' +
        'align-items: center;' +
        'z-index: 9999;' +
        'animation: fadeIn 0.3s ease-in-out;' +
      '}' +

      '@keyframes fadeIn {' +
        'from {' +
          'opacity: 0;' +
        '}' +
        'to {' +
          'opacity: 1;' +
        '}' +
      '}' +

      '.SpCarGal-container {' +
        'position: relative;' +
        'width:100%;' +
        'height:100%;' +
        'display: flex;' +
        'align-items: center;' +
        'justify-content: center;' +
      '}' +

      '.SpCarGal-content {' +
        'position: relative;' +
        'display: flex;' +
        'flex-direction: column;' +
        'align-items: center;' +
      '}' +

      '.SpCarGal-image {' +
        'max-width: 100%;' +
        'max-height: 85vh;' +
        'object-fit: contain;' +
        'border-radius: 4px;' +
      '}' +
      '.SpCarGal-alt {' +
        'color:#FFF;' +
        'font-size:1.5em;' +
        'padding:0.5em;' +
      '}' +

      '.SpCarGal-counter {' +
        'color: white;' +
        'margin-top: 15px;' +
        'font-size: 14px;' +
        'text-align: center;' +
      '}' +

      '.SpCarGal-close,' +
      '.SpCarGal-prev,' +
      '.SpCarGal-next {' +
        'position: absolute;' +
        'top: 20px;' +
        'z-index:10;' +
        'background-color:#333;' +
        'background-color:var(--couleur-principale);' +
        'color: white;' +
        'border: 1px solid #FFF;' +
        'padding: 10px 15px;' +
        'font-size: 28px !important;' +
        'cursor: pointer;' +
        'border-radius: 4px;' +
        'transition: background-color 0.3s ease;' +
        'height:auto;' +
      '}' +

      '.SpCarGal-close {' +
        'right: 20px;' +
        'top: 20px;' +
        'padding: 5px 12px;' +
        'font-size: 32px;' +
      '}' +

      '.SpCarGal-close:hover {' +
        'background-color: rgba(255, 255, 255, 0.5);' +
      '}' +

      '.SpCarGal-prev {' +
        'left: 20px;' +
        'top: 50%;' +
        'transform: translateY(-50%);' +
      '}' +

      '.SpCarGal-next {' +
        'right: 20px;' +
        'top: 50%;' +
        'transform: translateY(-50%);' +
      '}' +

      '.SpCarGal-prev:hover,' +
      '.SpCarGal-next:hover {' +
        'background-color: rgba(255, 255, 255, 0.5);' +
      '}' +

      '.SpCarGal-prev:disabled,' +
      '.SpCarGal-next:disabled {' +
        'opacity: 0.5;' +
        'cursor: not-allowed;' +
      '}' +

      '/* Responsive */' +
      '@media (max-width: 768px) {' +
        '.SpCarGal-prev,' +
        '.SpCarGal-next {' +
          'padding: 8px 12px;' +
          'font-size: 20px;' +
        '}' +

        '.SpCarGal-image {' +
          'max-height: 70vh;' +
        '}' +
      '}' +
    '</style>';

  // Injecter le CSS
  $('head').append(SpCarGalCSS);

  // Fonction pour afficher le carrousel
  function showSpCarGal(index) {
    currentIndex = index;
    var $overlay = $('.SpCarGal-overlay');

    $overlay.html(createSpCarGal());
    updateSpCarGalButtons();
  }

  // Fonction pour mettre à jour les boutons
  function updateSpCarGalButtons() {
    var $overlay = $('.SpCarGal-overlay');
    var $prevBtn = $overlay.find('.SpCarGal-prev');
    var $nextBtn = $overlay.find('.SpCarGal-next');

    // Désactiver/activer les boutons selon la position
    $prevBtn.prop('disabled', currentIndex === 0);
    $nextBtn.prop('disabled', currentIndex === imagesSpGallery.length - 1);

    // Mettre à jour l'image et le compteur
    $overlay.find('.SpCarGal-image').attr('src', imagesSpGallery[currentIndex].src).attr('alt', imagesSpGallery[currentIndex].alt);
    $overlay.find('.SpCarGal-content > div:nth-child(2)').text(imagesSpGallery[currentIndex].alt);
    $overlay.find('.current-index').text(currentIndex + 1);
  }

  // Gestionnaires d'événements
  $(document).on('click', '.SpCarGal-prev', function() {
    if (currentIndex > 0) {
      currentIndex--;
      updateSpCarGalButtons();
    }
  });

  $(document).on('click', '.SpCarGal-next', function() {
    if (currentIndex < imagesSpGallery.length - 1) {
      currentIndex++;
      updateSpCarGalButtons();
    }
  });

  // Fermeture
  $(document).on('click', '.SpCarGal-overlay', function(e) {
    if ($(e.target).hasClass('SpCarGal-overlay')) {
      $(this).remove();
    }
  });

  $(document).on('keydown', function(e) {
    if ($('.SpCarGal-overlay').length === 0) return;

    if (e.key === 'Escape') {
      $('.SpCarGal-overlay').remove();
    }
  });

  $(document).on('click', '.SpCarGal-close', function() {
    $('.SpCarGal-overlay').remove();
  });


  // Lancement du carousel au clic
  $('.sp-gallery-interaction img').on('click', function() {
    var clickedSrc = $(this).attr('src');
    var index = -1;
    for (var i = 0; i < imagesSpGallery.length; i++) {
      if (imagesSpGallery[i].src === clickedSrc) {
        index = i;
        break;
      }
    }

    if (index !== -1) {
      $('body').append(createSpCarGal());
      showSpCarGal(index);
    }
  });


});