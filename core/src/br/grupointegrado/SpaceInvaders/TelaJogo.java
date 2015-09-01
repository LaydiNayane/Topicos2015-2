package br.grupointegrado.SpaceInvaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FillViewport;

/**
 * Created by Laydi on 03/08/2015.
 */
public class TelaJogo extends TelaBase{

    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Stage palco;
    private Stage palcoInformacoes;
    private BitmapFont fonte;
    private Label lbPontuacao;
    private Label lbGameOver;
    private Image jogador;
    private Texture texturaJogador;
    private Texture texturaJogadorDireita;
    private Texture texturaJogadorEsquerda;
    private boolean indoDireita;
    private boolean indoEsquerda;
    private boolean indoCima;
    private boolean indoBaixo;
    private boolean atirando;
    private Array<Image> tiros = new Array<Image>();
    private Texture texturaTiros;
    private Texture texturaMeteoro1;
    private Texture texturaMeteoro2;
    private Array<Image> meteoros1 = new Array<Image>();
    private Array<Image> meteoros2 = new Array<Image>();

    private Array<Texture> texturasExplosao = new Array<Texture>();
    private Array<Explosao> explosoes = new Array<Explosao>();

    /**
     * construtor padrao da tela de jogo
     * @param game referencia para a classe principal
     */
    public TelaJogo(MainGame game) {
        super(game);
    }

    /**
     * chamado quando a tela é exibida
     */
    @Override
    public void show() {
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch = new SpriteBatch();
        palco = new Stage(new FillViewport(camera.viewportWidth, camera.viewportHeight, camera));
        palcoInformacoes = new Stage(new FillViewport(camera.viewportWidth, camera.viewportHeight, camera));

        initFonte();
        initInformacoes();
        initJogador();
        initTexturas();

    }

    private void initTexturas() {
        texturaTiros = new Texture("sprites/shot.png");
        texturaMeteoro1 = new Texture("sprites/enemie-1.png");
        texturaMeteoro2 = new Texture("sprites/enemie-2.png");

        for (int i = 1; i<= 17; i++){
            Texture text = new Texture("sprites/explosion-" + i + ".png");
            texturasExplosao.add(text);
        }
    }

    //instancia os objetos de Fonte
    private void initFonte(){
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/roboto.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter param =
                new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.color = Color.WHITE;
        param.size = 24;
        param.shadowOffsetX = 1;
        param.shadowOffsetY = 1;
        param.shadowColor = Color.BLUE;

        //fonte = new BitmapFont();
        fonte = generator.generateFont(param);

        generator.dispose();
    }

    private void initJogador() {
        texturaJogador = new Texture("sprites/player.png");
        texturaJogadorDireita = new Texture("sprites/player-right.png");
        texturaJogadorEsquerda = new Texture("sprites/player-left.png");

        jogador = new Image(texturaJogador);
        float x = camera.viewportWidth / 2 - jogador.getWidth() / 2;
        float y = 10;
        jogador.setPosition(x, y);
        palco.addActor(jogador);
    }

    private void initInformacoes(){
        Label.LabelStyle lbEstilo = new Label.LabelStyle();
        lbEstilo.font = fonte;
        lbEstilo.fontColor = Color.WHITE;

        lbPontuacao = new Label("0 pontos", lbEstilo);
        lbGameOver = new Label("Game Over", lbEstilo);
        lbGameOver.setVisible(false);
        palcoInformacoes.addActor(lbGameOver);
        palcoInformacoes.addActor(lbPontuacao);
    }

    /**
     * chamado a todo quadro de atualização do jogo (FPS)
     * @param delta tempo entre um quadro e outro (em segundos)
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(.15f, .15f, .25f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        lbPontuacao.setPosition(10, camera.viewportHeight - lbPontuacao.getPrefHeight() - 20);
        lbPontuacao.setText(pontuacao + " pontos");

        lbGameOver.setPosition(camera.viewportWidth / 2 - lbGameOver.getPrefWidth() / 2, camera.viewportHeight / 2);
        lbGameOver.setVisible(gameOver == true);

        atualizarExplosoes(delta);

        if(gameOver == false){
            capturaTeclas();
            atualizarJogador(delta);
            atualizarTiros(delta);
            atualizarMeteoros(delta);
            detectarColisoes(meteoros1, 5);
            detectarColisoes(meteoros2, 5);
        }

        palco.act(delta);// atualiza situacao do palco
        palco.draw(); // desenha o palco na tela
        palcoInformacoes.act(delta);// atualiza situacao do palco
        palcoInformacoes.draw();// desenha o palco na tela
    }

    private void atualizarExplosoes(float delta) {
        for (Explosao explosao : explosoes){
            // verifica se chegou ao fim
            if (explosao.getEstagio() >= 16){
                explosoes.removeValue(explosao, true);// remove a explosao do array
                explosao.getAtor().remove();//remove o ator do palco
            }else{
                //ainda nao chegou ao fim
                explosao.atualizar(delta);
            }
        }
    }

    private Rectangle recJogador = new Rectangle();
    private Rectangle recTiro = new Rectangle();
    private Rectangle recMeteoro = new Rectangle();

    private int pontuacao = 0;
    private boolean gameOver = false;

    private void detectarColisoes(Array<Image> meteoros, int valePonto) {
        recJogador.set(jogador.getX(), jogador.getY(), jogador.getWidth(), jogador.getHeight());
        for (Image meteoro : meteoros){
            recMeteoro.set(meteoro.getX(), meteoro.getY(), meteoro.getWidth(), meteoro.getHeight());
            // detecta colisoes com os tiros
            for (Image tiro : tiros){
               recTiro.set(tiro.getX(), tiro.getY(), tiro.getWidth(), tiro.getHeight());

                if(recMeteoro.overlaps(recTiro)){
                    // aqui ocorre uma colisao do tiro com o meteoro1
                    pontuacao += valePonto;
                    tiro.remove();//remove do palco
                    tiros.removeValue(tiro, true);//remove da lista
                    meteoro.remove();//remove do palco
                    meteoros.removeValue(meteoro, true);//remove da lista
                    criarExplosao(meteoro.getX(), meteoro.getY());
                }
            }

            // detecta colisoes com o player
            if (recJogador.overlaps(recMeteoro)){
                //ocorre colisao de jogador com meteoro 1
                gameOver = true;
            }
        }

    }

    // cria a explosao na posicao x e y
    private void criarExplosao(float x, float y) {
        Image ator = new Image(texturasExplosao.get(0));
        ator.setPosition(x, y);
        palco.addActor(ator);

        Explosao explosao = new Explosao(ator, texturasExplosao);
        explosoes.add(explosao);
    }

    private void atualizarMeteoros(float delta) {
        int qtdMeteoros = meteoros1.size + meteoros2.size; // retorna qtd de meteoros criados

        if (qtdMeteoros < 10){

        int tipo = MathUtils.random(1, 4);// retorna 1 ou 2 aleatoriamente

        if(tipo == 1){
            // cria meteoro 1
            Image meteoro = new Image(texturaMeteoro1);
            float x = MathUtils.random(0, camera.viewportWidth - meteoro.getWidth());
            float y = MathUtils.random(camera.viewportHeight, camera.viewportHeight * 2);
            meteoro.setPosition(x, y);
            meteoros1.add(meteoro);
            palco.addActor(meteoro);
        }else if (tipo == 2){
            // cria meteoro 2
            Image meteoro = new Image(texturaMeteoro2);
            float x = MathUtils.random(0, camera.viewportWidth - meteoro.getWidth());
            float y = MathUtils.random(camera.viewportHeight, camera.viewportHeight * 2);
            meteoro.setPosition(x, y);
            meteoros2.add(meteoro);
            palco.addActor(meteoro);
        }
        }
        float velocidade1 = 100;// 200 pixels por segundo
        for(Image meteoro : meteoros1){
            float x = meteoro.getX();
            float y = meteoro.getY() - velocidade1 * delta;
            meteoro.setPosition(x, y);
            if (meteoro.getY() + meteoro.getHeight() < 0 ){
                meteoro.remove(); // remove do palco
                meteoros1.removeValue(meteoro, true);// remove da lista

            }
        }

        float velocidade2 = 150;// 250 pixels por segundo
        for(Image meteoro : meteoros2){
            float x = meteoro.getX();
            float y = meteoro.getY() - velocidade2 * delta;
            meteoro.setPosition(x, y);
            if (meteoro.getY() + meteoro.getHeight() < 0 ){
                meteoro.remove(); // remove do palco
                meteoros2.removeValue(meteoro, true);// remove da lista

            }
        }

    }

    private float intervaloTiros = 0; // tempo acumulado entre os tiros
    private final float MIN_INTERVALO_TIROS = 0.4f; // minimo de tempo entre os tiros

    private void atualizarTiros(float delta) {
        // cria um novo tiro se necessario
        intervaloTiros = intervaloTiros + delta; // acumula o tempo percorrido
        if(atirando){
            if(intervaloTiros >= MIN_INTERVALO_TIROS){ // verifica se o tempo minimo foi atingido
                Image tiro = new Image(texturaTiros);
                float x = jogador.getX() + jogador.getWidth() / 2 - tiro.getWidth() / 2;
                float y = jogador.getY() + jogador.getHeight();
                tiro.setPosition(x, y);
                tiros.add(tiro);
                palco.addActor(tiro);
                intervaloTiros = 0;
            }

        }
        float velocidade = 200; // velocidade de movimentacao do tiro
        for(Image tiro : tiros){ // percorre todos os tiros existentes
            //movimenta o tiro em direcao ao topo
            float x = tiro.getX();
            float y = tiro.getY() + velocidade * delta;
            tiro.setPosition(x, y);
            //remove os tiros que sairam de posicao da tela
            if(tiro.getY() > camera.viewportHeight){
                tiros.removeValue(tiro, true); // remove da lista
                tiro.remove();// remove do palco
            }
        }
    }

    /**
     * Atualiza a posicao do jogador
     * @param delta
     */
    private void atualizarJogador(float delta) {
        float velocidade = 200; //Velocidade de movimento do jogador
        if (indoDireita) {

            if (jogador.getX() < camera.viewportWidth - jogador.getWidth()) {
                float x = jogador.getX() + velocidade * delta;
                float y = jogador.getY();
                jogador.setPosition(x, y);

            }
        }
        if (indoEsquerda) {
            if (jogador.getX() > 0) {
                float x = jogador.getX() - velocidade * delta;
                float y = jogador.getY();
                jogador.setPosition(x, y);

            }
        }
        if (indoCima) {
            float x = jogador.getX();
            float y = jogador.getY() + velocidade * delta;
            jogador.setPosition(x, y);

        }

        if (indoBaixo) {
            float x = jogador.getX();
            float y = jogador.getY() - velocidade * delta;
            jogador.setPosition(x, y);


        }
        if (indoDireita) {
            //trocar imagem direita
            jogador.setDrawable(new SpriteDrawable(new Sprite(texturaJogadorDireita)));
        }else if (indoEsquerda) {
            //trocar imagem esqueda
            jogador.setDrawable(new SpriteDrawable(new Sprite(texturaJogadorEsquerda)));
        }else {
            //trocar imagem cena
            jogador.setDrawable(new SpriteDrawable(new Sprite(texturaJogador)));
        }

    }

    /**
     * Verifica se as teclas estao pressionadas
     */
    private void capturaTeclas() {
        indoDireita = false;
        indoEsquerda = false;
        //indoCima = false;
        //indoBaixo = false;
        atirando = false;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            indoEsquerda = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            indoDireita = true;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            atirando = true;
        }


        //if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
          //  indoCima = true;
        //}
        //if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
          //  indoBaixo = true;
        //}
    }

    /**
     * chamado sempre que ha uma alteração no tamanho da tela
     * @param width novo valor de largura da tela
     * @param height novo valor de altura da tela
     */
    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        camera.update();
    }

    /**
     * chamado sempre que o jogo for minimizado
     */
    @Override
    public void pause() {

    }

    /**
     * chamado sempre que o jogo voltar para o primeiro plano
     */
    @Override
    public void resume() {

    }

    /**
     * chamado quando a tela for destruida
     */
    @Override
    public void dispose() {
        batch.dispose();
        palco.dispose();
        palcoInformacoes.dispose();
        fonte.dispose();
        texturaJogador.dispose();
        texturaJogadorDireita.dispose();
        texturaJogadorEsquerda.dispose();
        texturaTiros.dispose();
        texturaMeteoro1.dispose();
        texturaMeteoro2.dispose();
        for (Texture text : texturasExplosao){
            text.dispose();
        }

    }
}
