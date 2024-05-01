package com.example.blog_app_springboot.articles;

import com.example.blog_app_springboot.articles.dtos.CreateArticleRequest;
import com.example.blog_app_springboot.users.UserRepository;
import com.example.blog_app_springboot.users.UserService;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class ArticleService {
    private ArticleRepository articleRepository;
    private UserRepository userRepository;

    public ArticleService(ArticleRepository articleRepository, UserRepository userRepository) {
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
    }

    public Iterable<ArticleEntity> getAllArticles() {
        return articleRepository.findAll();
    }
    public ArticleEntity getArticleBySlug(String Slug) {
        var article = articleRepository.findBySlug(Slug);
        if (article == null) throw new ArticleNotFoundException(Slug);

        return article;
    }
    public ArticleEntity createArticle(CreateArticleRequest request, Long authorID) {
        var author = userRepository.findById(authorID).orElseThrow(()-> new UserService.UserNotFoundException(authorID));
        var newArticle = ArticleEntity.builder()
                .title(request.getTitle())
                // TODO: create a proper slug function
                .slug(request.getTitle().toLowerCase().replaceAll("\\s+","-"))
                .body(request.getBody())
                .author(author)
                .subtitle(request.getSubtitle()).build();

        return articleRepository.save(newArticle);
    }
    public ArticleEntity updateArticle(Long articleID, CreateArticleRequest request){
        var article = articleRepository.findById(articleID).orElseThrow(()-> new ArticleNotFoundException(articleID));
        if(request.getTitle() != null){
            article.setTitle(request.getTitle());
            article.setSlug(request.getTitle().toLowerCase().replaceAll("\\s+","-"));
        }
        if(request.getBody() != null){ article.setBody(request.getBody());}
        if(request.getSubtitle() != null){ article.setSubtitle(request.getSubtitle());}
        return articleRepository.save(article);
    }

    static class ArticleNotFoundException extends IllegalArgumentException {
        public ArticleNotFoundException(String message) {
            super("The slug "+message+ " is not found");
        }
        public ArticleNotFoundException(Long articleID) {
            super("The ID: "+articleID+ " is not found");
        }
    }
}
