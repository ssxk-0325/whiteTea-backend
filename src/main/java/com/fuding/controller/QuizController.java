package com.fuding.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fuding.common.Result;
import com.fuding.entity.QuizAnswer;
import com.fuding.entity.QuizQuestion;
import com.fuding.service.QuizService;
import com.fuding.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 趣味问答控制器
 */
@RestController
@RequestMapping("/quiz")
@CrossOrigin
public class QuizController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取问题列表
     */
    @GetMapping("/list")
    public Result<IPage<QuizQuestion>> getQuestionList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        try {
            // MyBatis-Plus 的 Page 从 1 开始，前端传的是从 0 开始，需要 +1
            Page<QuizQuestion> questionPage = new Page<>(page + 1, size);
            Long userId = null;
            // 尝试获取用户ID（如果已登录）
            try {
                String token = request.getHeader("Authorization");
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }
                userId = jwtUtil.getUserIdFromToken(token);
            } catch (Exception e) {
                // 未登录，userId保持为null
            }
            IPage<QuizQuestion> result = quizService.getQuestionList(questionPage, category, difficulty, keyword, userId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取问题详情
     */
    @GetMapping("/{id}")
    public Result<QuizQuestion> getQuestionDetail(@PathVariable Long id) {
        try {
            QuizQuestion question = quizService.getQuestionById(id);
            return Result.success(question);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 提交答案
     */
    @PostMapping("/{id}/answer")
    public Result<QuizAnswer> submitAnswer(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            Integer userAnswer = Integer.valueOf(params.get("userAnswer").toString());
            QuizAnswer answer = quizService.submitAnswer(userId, id, userAnswer);
            return Result.success("提交成功", answer);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取我的答题记录
     */
    @GetMapping("/my-answers")
    public Result<List<QuizAnswer>> getMyAnswers(
            @RequestParam(required = false) Integer category,
            HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            List<QuizAnswer> answers = quizService.getUserAnswers(userId, category);
            return Result.success(answers);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取我的答题统计
     */
    @GetMapping("/my-statistics")
    public Result<Map<String, Object>> getMyStatistics(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            Map<String, Object> stats = quizService.getUserStatistics(userId);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ========== 管理员接口 ==========

    /**
     * 管理员获取问题列表
     */
    @GetMapping("/admin/list")
    public Result<IPage<QuizQuestion>> adminGetQuestionList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            jwtUtil.getUserIdFromToken(token); // 验证token有效性

            // MyBatis-Plus 的 Page 从 1 开始，前端传的是从 0 开始，需要 +1
            Page<QuizQuestion> questionPage = new Page<>(page + 1, size);
            IPage<QuizQuestion> result = quizService.getAdminQuestionList(questionPage, category, difficulty, keyword);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员创建问题
     */
    @PostMapping("/admin/create")
    public Result<QuizQuestion> adminCreateQuestion(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            jwtUtil.getUserIdFromToken(token); // 验证token有效性

            QuizQuestion question = quizService.createQuestion(params);
            return Result.success("创建成功", question);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员更新问题
     */
    @PutMapping("/admin/update")
    public Result<QuizQuestion> adminUpdateQuestion(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            jwtUtil.getUserIdFromToken(token); // 验证token有效性

            QuizQuestion question = quizService.updateQuestion(params);
            return Result.success("更新成功", question);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员删除问题
     */
    @DeleteMapping("/admin/{id}")
    public Result<Void> adminDeleteQuestion(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            jwtUtil.getUserIdFromToken(token); // 验证token有效性

            quizService.deleteQuestion(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

